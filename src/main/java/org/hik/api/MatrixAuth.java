package org.hik.api;

import com.sun.net.httpserver.HttpServer;
import org.hik.api.auth.AuthMetadata;
import org.hik.api.auth.Tokens;
import org.hik.api.auth.WhoAmI;
import org.hik.context.DiscoveryResponse;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.utils.HttpTransport;
import org.hik.services.utils.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MatrixAuth implements Auth {

    private final Logger logger = LoggerFactory.getLogger(MatrixAuth.class);
    private final HttpTransport httpTransport = new HttpTransport(10);
    private final Random random = new SecureRandom();
    private final URI baseUrl;

    public MatrixAuth(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    private static String generateCodeChallenge(String codeVerifier) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    private static String extractQueryParam(String query, String key) {
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals(key)) {
                return java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    @Override
    public AuthMetadata getAuthMetadata() {
        DiscoveryResponse discoveryResponse = this.fetchWellKnown();
        var uri = httpTransport.generateEncodedURI(discoveryResponse.homeserver().baseUrl(),
                "/_matrix/client/v1/auth_metadata", null);
        var responseBody = httpTransport.getEvent(uri, null);
        return Mapper.getObjectFromString(responseBody, AuthMetadata.class);
    }

    @Override
    public WhoAmI getCurrentAccountInformation() {
        throw new UnsupportedOperationException("getCurrentAccountInformation is not yet implemented");
    }

    /// Method used to obtain the .well-known data and store the base url.
    ///
    /// @return a [DiscoveryResponse] with data.
    /// @throws IllegalArgumentException when the homeserver url violates RFC 2396 or is null
    /// @throws MatrixIOException        when the payload cannot be processed
    public DiscoveryResponse fetchWellKnown() {
        try {
            URI uri = URI.create(baseUrl + "/.well-known/matrix/client");
            var response = httpTransport.getEvent(uri, null);
            return Mapper.getObjectFromString(response, DiscoveryResponse.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix discovery JSON", e);
        }
    }

    /// Runs the full MSC2965/2966/2967 OAuth 2.0 flow: discovery, dynamic client
    /// registration, PKCE authorization via a loopback callback server, and token exchange.
    ///
    /// @param clientName the client name
    /// @param port       the port connection
    /// @param deviceId   the device id
    /// @return a [Tokens] pair holding the access and refresh tokens.
    /// @throws MatrixIOException when a network or parsing step fails.
    public Tokens login(String clientName, int port, String deviceId) {
        // We get the auth metadata
        var metadata = this.getAuthMetadata();

        // Create our redirect
        String redirectUri = "http://127.0.0.1:" + port + "/callback";

        // Encode the endpoint parameters to register
        Map<String, Object> map = new HashMap<>();
        map.put("client_name", clientName);
        map.put("redirect_uris", List.of(redirectUri));
        map.put("grant_types", List.of("authorization_code", "refresh_token"));
        map.put("token_endpoint_auth_method", "none");
        map.put("application_type", "native");
        map.put("client_uri", "https://github.com/1hiking/JavaMatrixLibrary");
        var mappedInput = Mapper.createObjectFromMap(map);

        // Send the payload using the aforementioned record obtained and get the client_id
        var responseBody = httpTransport.postEvent(metadata.registrationEndpoint(), mappedInput, null);
        var clientId = Mapper.getStringFromSingleObject(responseBody, "client_id");


        // We generate values
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = generateCodeChallenge(codeVerifier);
        String state = generateRandomUrlSafeString(24);
        String scope = "urn:matrix:org.matrix.msc2967.client:api:* urn:matrix:org.matrix.msc2967.client:device:" + deviceId;

        Map<String, Object> mapAuth = new HashMap<>();
        mapAuth.put("client_id", clientId);
        mapAuth.put("response_type", "code");
        mapAuth.put("response_mode", "query");
        mapAuth.put("scope", scope);
        mapAuth.put("state", state);
        mapAuth.put("code_challenge", codeChallenge);
        mapAuth.put("code_challenge_method", "S256");
        // Send the payload, we don't encode the parameters
        // https://spec.matrix.org/v1.19/client-server-api/#authorisation-code-flow
        var uriAuth = httpTransport.generateRawURI(metadata.authorizationEndpoint().toString(),
                metadata.authorizationEndpoint().getPath(), mapAuth);

        CompletableFuture<String> authorizationCode = new CompletableFuture<>();

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.createContext("/callback", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String returnedState = extractQueryParam(query, "state");
            String code = extractQueryParam(query, "code");
            String error = extractQueryParam(query, "error");

            String responseBodyCallback;
            if (error != null) {
                responseBodyCallback = "Authorization failed: " + error;
                authorizationCode.completeExceptionally(new IOException(responseBodyCallback));
            } else if (!state.equals(returnedState)) {
                responseBodyCallback = "State mismatch; possible CSRF, aborting.";
                authorizationCode.completeExceptionally(new IOException(responseBodyCallback));
            } else {
                authorizationCode.complete(code);
                responseBodyCallback = "Login complete. You can close this tab and return to the app.";

            }

            byte[] bytes = responseBodyCallback.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        String code;
        try {
            logger.info("URI AUTH: {}", uriAuth);
            openBrowser(uriAuth);
            code = awaitAuthorizationCode(authorizationCode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            server.stop(1);
        }

        String tokenRequestBody = "grant_type=authorization_code"
                + "&code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&code_verifier=" + URLEncoder.encode(codeVerifier, StandardCharsets.UTF_8);

        var tokenRes = httpTransport.postEventAuth(metadata.tokenEndpoint(), tokenRequestBody);

        var access = Mapper.getStringFromSingleObject(tokenRes, "access_token");
        var refresh = Mapper.getStringFromSingleObject(tokenRes, "refresh_token");
        return new Tokens(access, refresh);
    }

    /// Blocks until the loopback server's `/callback` handler completes the future,
    /// converting interruption and callback failure into the SDK's own exception hierarchy.
    private String awaitAuthorizationCode(CompletableFuture<String> authorizationCode) {
        try {
            return authorizationCode.get(5, TimeUnit.MINUTES); // time out if the user does not proceed, for example if the user closes the website.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixIOException(
                    "Login was interrupted while waiting for the authorization callback", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new MatrixIOException("Authorization callback failed", cause);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    private String generateCodeVerifier() {
        byte[] randomBytes = new byte[32];
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String generateRandomUrlSafeString(int numBytes) {
        byte[] randomBytes = new byte[numBytes];
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private void openBrowser(URI url) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(url);
        } else {
            logger.warn("Could not auto-open a browser. Open this URL manually: {}", url);
        }
    }


}