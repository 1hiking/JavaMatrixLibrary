package org.hik.api;

import org.hik.context.ClientContext;
import org.hik.context.DiscoveryResponse;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.modules.Events;
import org.hik.services.modules.Room;
import org.hik.services.modules.UserData;
import org.hik.services.networking.HttpTransport;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

/// A [MatrixClient] provides all the functionality required to interact with a Matrix compliant server.
public class MatrixClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClientCredentials credentials;
    private final HttpTransport httpTransport = new HttpTransport();
    private final DiscoveryResponse discoveryResponse;
    private final Events events;
    private final Room room;
    private final UserData userData;

    private MatrixClient(String unprocessedBaseUrl, String username, String authToken) throws InterruptedException {
        this.credentials = new ClientCredentials(unprocessedBaseUrl, username, authToken);
        this.discoveryResponse = fetchWellKnown();
        var context = new ClientContext(this.credentials, this.discoveryResponse);
        this.events = new Events(context);
        this.room = new Room(context);
        this.userData = new UserData(context);
    }

    public Events events() {
        return this.events;
    }

    public Room room() {
        return this.room;
    }

    public UserData userData() {
        return this.userData;
    }

    /// Default factory, which will make the initial payloads to request necessary data for further requests
    ///
    /// @param unprocessedBaseUrl the full qualified url of the server.
    /// @param username           the username assigned to a registered account.
    /// @param authToken          a valid non-expired auth token.
    /// @return an authenticated client.
    /// @throws InterruptedException when the HTTP Client is interrupted
    public static MatrixClient create(String unprocessedBaseUrl, String username, String authToken) throws InterruptedException {
        return new MatrixClient(unprocessedBaseUrl, username, authToken);
    }

    /// Method used to obtain the .well-known data and store the base url.
    ///
    /// @throws IllegalArgumentException when the homeserver url violates RFC 2396 or is null
    /// @throws MatrixIOException        when the payload cannot be processed
    /// @throws InterruptedException     when the HTTP Client is interrupted
    private DiscoveryResponse fetchWellKnown() throws InterruptedException {
        try {
            URI uri = URI.create(credentials.baseUrl() + "/.well-known/matrix/client");
            var response = httpTransport.getEvent(uri, null);
            return objectMapper.readValue(response, DiscoveryResponse.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix discovery JSON", e);
        } catch (IOException e) {
            throw new MatrixIOException("Network error during Matrix discovery", e);
        }
    }
}