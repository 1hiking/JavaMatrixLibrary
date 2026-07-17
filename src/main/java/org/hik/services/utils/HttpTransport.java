package org.hik.services.utils;

import org.hik.exceptions.ErrorResponse;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import tools.jackson.core.exc.StreamReadException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// [HttpTransport] handles all network-related tasks shared across the library,
/// including issuing requests, processing responses, and URI encoding.
///
/// Unless otherwise documented, requests expect JSON bodies as [String]s and
/// return unprocessed response bodies as [String]s, callers are responsible
/// for their own (de)serialization.
///
/// Failed requests are validated against the server's response and throw
/// [MatrixNetworkException], populated with the HTTP status code and any
/// error message returned by the server, and [MatrixIOException] if the server
/// JSON response wasn't even sent.
public class HttpTransport {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private final HttpClient client;

    public HttpTransport(int timeOut) {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeOut)).build();
    }

    private void validateResponse(int code, String body) {
        if (code >= 200 && code < 300) {
            return;
        }

        if (body.isBlank()) {
            throw new MatrixNetworkException("Server returned with unknown error");
        }

        ErrorResponse errorResponse;
        try {
            errorResponse = Mapper.getObjectFromString(body, ErrorResponse.class);
        } catch (StreamReadException e) {
            throw new MatrixIOException("Server returned with malformed response", e);
        }

        throw new MatrixNetworkException("Server returned with error: " + errorResponse.error() + ", and code: " + errorResponse.errCode());
    }

    /// Sends a `GET` request to the given endpoint.
    ///
    /// @param path      the [URI] of the endpoint to `GET`.
    /// @param authToken if supplied, the `Bearer` token.
    /// @return a JSON [String].
    /// @throws IllegalArgumentException if the path was not supplied
    public String getEvent(URI path, String authToken) {
        var builderRequest = HttpRequest.newBuilder()
                .uri(path)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .GET();

        if (authToken != null) {
            builderRequest.header(AUTHORIZATION, BEARER + authToken);
        }
        var request = builderRequest.build();


        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();

    }


    /// Sends a `POST` request to the given endpoint.
    ///
    /// @param path      the [URI] of the endpoint to `POST`.
    /// @param body      a JSON [String].
    /// @param authToken if supplied, the `Bearer` token.
    /// @return a JSON [String].
    /// @throws MatrixIOException      if an I/O error has occurred while sending the request
    /// @throws MatrixNetworkException if the path was not supplied
    public String postEvent(URI path, String body, String authToken) {
        var builderRequest = HttpRequest.newBuilder()
                .uri(path);

        if (body != null) {
            builderRequest.header(CONTENT_TYPE, APPLICATION_JSON);
        }

        builderRequest.POST(body != null ? HttpRequest.BodyPublishers.ofString(body) :
                HttpRequest.BodyPublishers.noBody());

        if (authToken != null) {
            builderRequest.header(AUTHORIZATION, BEARER + authToken);
        }
        var request = builderRequest.build();


        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();

    }

    /// Sends a `POST` request to the given endpoint.
    ///
    /// @param path the [URI] of the endpoint to query.
    /// @param body a JSON [String].
    /// @return a JSON [String].
    /// @throws MatrixIOException      if an I/O error has occurred while sending the request
    /// @throws MatrixNetworkException if the path was not supplied
    public String postEventAuth(URI path, String body) {
        var builderRequest = HttpRequest.newBuilder()
                .uri(path);


        builderRequest.header(CONTENT_TYPE, "application/x-www-form-urlencoded");

        builderRequest.POST(body != null ? HttpRequest.BodyPublishers.ofString(body) :
                HttpRequest.BodyPublishers.noBody());

        var request = builderRequest.build();


        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();

    }


    /// Sends a `PUT` request to the given endpoint.
    ///
    /// @param path      the [URI] of the endpoint to query.
    /// @param body      a JSON [String]
    /// @param authToken if supplied, the `Bearer` token.
    /// @return a JSON [String] when the operation is successful.
    /// @throws MatrixIOException        if an I/O error has occurred while sending the request
    /// @throws MatrixNetworkException   if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String putEvent(URI path, String body, String authToken) {


        var builderRequest = HttpRequest.newBuilder()
                .uri(path)
                .headers(AUTHORIZATION, BEARER + authToken, CONTENT_TYPE, APPLICATION_JSON);

        builderRequest.PUT(body != null ? HttpRequest.BodyPublishers.ofString(body) :
                HttpRequest.BodyPublishers.noBody());
        var request = builderRequest.build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();
    }


    /// Sends a `PUT` request to the given endpoint.
    ///
    /// @param path      the [URI] of the endpoint to query.
    /// @param resource  a [Path] pointing to the resource to be uploaded.
    /// @param authToken if supplied, the `Bearer` token.
    /// @return a JSON [String].
    /// @throws MatrixIOException        if an I/O error has occurred while sending the request
    /// @throws MatrixNetworkException   if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String putResource(URI path, Path resource, String authToken) {
        HttpRequest uploadRequest;
        try {
            uploadRequest = HttpRequest.newBuilder()
                    .uri(path)
                    .headers(AUTHORIZATION, BEARER + authToken, CONTENT_TYPE, Files.probeContentType(resource))
                    .PUT(HttpRequest.BodyPublishers.ofFile(resource))
                    .build();
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        }

        HttpResponse<String> response;
        try {
            response = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();

    }

    /// Sends a `DELETE` request to the given endpoint.
    ///
    /// @param path      the [URI] of the endpoint to query.
    /// @param authToken if supplied, the `Bearer` token.
    /// @return a JSON [String].
    /// @throws MatrixIOException        if an I/O error has occurred while sending the request
    /// @throws MatrixNetworkException   if the operation has been interrupted
    /// @throws IllegalArgumentException if the path was not supplied
    public String deleteEvent(URI path, String authToken) {
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(path)
                .header(AUTHORIZATION, BEARER + authToken)
                .DELETE()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MatrixIOException("There has been an I/O error attempting to process this request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MatrixNetworkException("This request has been interrupted", e);
        }
        this.validateResponse(response.statusCode(), response.body());
        return response.body();
    }


    /// URL-encodes a string using UTF-8.
    ///
    /// @param value the string to encode
    /// @return the URL-encoded string
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /// Builds a [URI] from a base, path, and query parameters, with the path and
    /// query percent-encoded to UTF-8.
    ///
    /// @param baseUrl the base [URI] containing a schema and an authority.
    /// @param path    the path, for example: `/_matrix/client/v3/join/!room:example.org`
    /// @param params  query parameters; accepts wrapped primitives and Lists for
    ///                repeated parameters. Null values, null list items, or a null/empty
    ///                map are all safely ignored.
    /// @return a safe, fully composed [URI]
    public URI generateEncodedURI(String baseUrl, String path, Map<String, Object> params) {
        String query = encodeQueryParams(params);
        try {
            URI base = URI.create(baseUrl);
            return new URI(base.getScheme(), base.getAuthority(), path, query.isEmpty() ? null : query, null);
        } catch (URISyntaxException e) {
            throw new MatrixIOException("Failure parsing URI", e);
        }
    }


    /// Builds a [URI] from a base, path, and query parameters.
    /// This method WON'T encode to UTF-8 the queries
    ///
    /// @param baseUrl the base [URI] containing a schema and an authority.
    /// @param path    the path, for example: `/_matrix/client/v3/join/!room:example.org`
    /// @param params  query parameters; accepts wrapped primitives and Lists for
    ///                repeated parameters. Null values, null list items, or a null/empty
    ///                map are all safely ignored.
    /// @return a safe, fully composed [URI]
    public URI generateRawURI(String baseUrl, String path, Map<String, Object> params) {
        String query = rawQueryParams(params);
        try {
            URI base = URI.create(baseUrl);
            return new URI(base.getScheme(), base.getAuthority(), path, query.isEmpty() ? null : query, null);
        } catch (URISyntaxException e) {
            throw new MatrixIOException("Failure parsing URI", e);
        }
    }

    private String encodeQueryParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return "";
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .flatMap(e -> valuesOf(e.getValue()).map(v -> encode(e.getKey()) + "=" + encode(v.toString())))
                .collect(Collectors.joining("&"));
    }

    private String rawQueryParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return "";
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .flatMap(e -> valuesOf(e.getValue()).map(v -> e.getKey() + "=" + v.toString()))
                .collect(Collectors.joining("&"));
    }

    private Stream<?> valuesOf(Object value) {
        return value instanceof List<?> list
                ? list.stream().filter(Objects::nonNull)
                : Stream.of(value);
    }

}
