package org.hik.api;

import org.hik.context.ClientContext;
import org.hik.context.DiscoveryResponse;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.events.EventService;
import org.hik.services.rooms.RoomService;
import org.hik.services.userdata.UserDataService;
import org.hik.services.utils.ConfigurationMapper;
import org.hik.services.utils.HttpTransport;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;

/// A [MatrixClient] provides all the functionality required to interact with a Matrix compliant server.
public class MatrixClient {
    private final ObjectMapper objectMapper = ConfigurationMapper.getInstance();
    private final ClientCredentials credentials;
    private final HttpTransport httpTransport = new HttpTransport();
    private final Event event;
    private final Room roomService;
    private final UserData userDataService;

    private MatrixClient(String unprocessedBaseUrl, String username, String authToken) {
        this.credentials = new ClientCredentials(unprocessedBaseUrl, username, authToken);
        DiscoveryResponse discoveryResponse = fetchWellKnown();
        var context = new ClientContext(this.credentials, discoveryResponse);
        this.event = new EventService(context);
        this.roomService = new RoomService(context);
        this.userDataService = new UserDataService(context);
    }

    public Event events() {
        return this.event;
    }

    public Room room() {
        return this.roomService;
    }

    public UserData userData() {
        return this.userDataService;
    }

    /// Default factory, which will make the initial payloads to request necessary data for further requests
    ///
    /// @param unprocessedBaseUrl the full qualified url of the server.
    /// @param username           the username assigned to a registered account.
    /// @param authToken          a valid non-expired auth token.
    /// @return an authenticated client.
    public static MatrixClient create(String unprocessedBaseUrl, String username, String authToken) {
        return new MatrixClient(unprocessedBaseUrl, username, authToken);
    }

    /// Method used to obtain the .well-known data and store the base url.
    ///
    /// @throws IllegalArgumentException when the homeserver url violates RFC 2396 or is null
    /// @throws MatrixIOException        when the payload cannot be processed
    private DiscoveryResponse fetchWellKnown() {
        try {
            URI uri = URI.create(credentials.baseUrl() + "/.well-known/matrix/client");
            var response = httpTransport.getEvent(uri, null);
            return objectMapper.readValue(response, DiscoveryResponse.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix discovery JSON", e);
        }
    }
}