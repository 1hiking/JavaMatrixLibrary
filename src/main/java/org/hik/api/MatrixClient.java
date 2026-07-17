package org.hik.api;

import org.hik.context.ClientContext;
import org.hik.context.DiscoveryResponse;
import org.hik.services.events.EventService;
import org.hik.services.rooms.RoomService;
import org.hik.services.userdata.UserDataService;

/// A [MatrixClient] provides all the functionality required to interact with a Matrix compliant server.
public class MatrixClient {
    private final Event event;
    private final Room roomService;
    private final UserData userDataService;

    private MatrixClient(DiscoveryResponse discoveryResponse, String authToken) {
        var context = new ClientContext(authToken, discoveryResponse);
        this.event = new EventService(context);
        this.roomService = new RoomService(context);
        this.userDataService = new UserDataService(context);
    }

    /// Exposes the underlying [Event] for operations.
    ///
    /// @return the underlying [Event] instance.
    public Event events() {
        return this.event;
    }

    /// Exposes the underlying [Room] for operations.
    ///
    /// @return the underlying [Room] instance.
    public Room room() {
        return this.roomService;
    }

    /// Exposes the underlying [UserData] for operations.
    ///
    /// @return the underlying [UserData] instance.
    public UserData userData() {
        return this.userDataService;
    }

    /// Default factory, which will make the initial payloads to request necessary data for further requests
    ///
    /// @param discoveryResponse [DiscoveryResponse] of a server.
    /// @param authToken         a valid non-expired auth token.
    /// @return an authenticated client.
    public static MatrixClient create(DiscoveryResponse discoveryResponse, String authToken) {
        return new MatrixClient(discoveryResponse, authToken);
    }


}