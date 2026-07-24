package org.hik.api;

import org.hik.api.events.*;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;

import java.nio.file.Path;
import java.util.List;

/// Core interface for executing Matrix protocol operations against room events.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see <a href="https://spec.matrix.org/latest/client-server-api/#events">Matrix Client-Server API Specification for Events</a>
public interface Event {

    /// Gets an event from a room.
    ///
    /// @param roomId  the room ID where the event is.
    /// @param eventId the event ID to retrieve.
    /// @return the full event.
    ClientEvent getEvent(String roomId, String eventId);

    /// Returns currently-joined members
    ///
    /// @param roomId the room ID to fetch data from.
    /// @return a list of room members.
    RoomMembers getJoinedMembers(String roomId);

    /// Returns a filterable list of members and their current membership state in a room.
    ///
    /// @param roomId        the room ID to fetch data from.
    /// @param at            the point in time (pagination token) to return members for in the room. This token can be obtained from a `prev_batch` token returned for each room by the sync API.
    /// @param membership    the kind of membership to filter for. When specified alongside notMembership, the two parameters create an `or` condition
    /// @param notMembership the kind of membership to exclude from the results. Defaults to no filtering if unspecified.
    /// @return a list of [ClientEvent]s with the membership information of room members.
    List<ClientEvent> getMembers(String roomId, String at, Membership membership, Membership notMembership);

    /// Get the state events for the current state of a room.
    ///
    /// @param roomId the room ID to fetch data from.
    /// @return the current state of the room
    List<ClientEvent> getStateEvents(String roomId);

    /// Looks up the contents of a state event in a room. If the user is joined to the room then the state is taken from the current state of the room.
    /// If the user has left the room then the state is taken from the state of the room when they left.
    ///
    /// @param roomId    the room ID to fetch data from.
    /// @param eventType the type of state to look up.
    /// @param stateKey  the room to look up the state in.
    /// @param format    the key of the state to look up. Defaults to an empty string. When an empty string, the trailing slash on this endpoint is optional.
    /// @return the content of the state event, or the entire client-formatted event if `format` as [Format#EVENT] was used.
    List<ClientEvent> getStateEvents(String roomId, String eventType, String stateKey, Format format);

    /// Returns a list of message and state events for a room. It uses pagination query parameters to paginate
    /// history in the room.
    /// The content is not parsed or escaped which means newlines (`\n`) and such escape sequences will not be parsed.
    ///
    /// @param roomId the room ID to fetch data from.
    /// @param params the [QueryParametersMessages] for the operation.
    /// @param dir    the [ChronologicalDirection] in which to search
    /// @return [Messages] with available data.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    Messages getMessages(String roomId, ChronologicalDirection dir, QueryParametersMessages params);

    /// Gets an event from a room closest to the given timestamp, in the direction specified by the `dir` parameter.
    ///
    /// @param roomId    the room ID to fetch data from.
    /// @param dir       the [ChronologicalDirection] in which to search
    /// @param timestamp the timestamp to search from, as given in milliseconds since the Unix epoch.
    /// @return [EventMetadata] if an event was found.
    EventMetadata getEventClosestToTimestamp(String roomId, ChronologicalDirection dir, int timestamp);

    /// Get a copy of the current state and the most recent messages in a room.
    /// Exclusively used for "peeking", otherwise use [#sync(QueryParametersSync)].
    ///
    /// @param roomId the room ID to fetch data from.
    /// @return [RoomInfo] with current state of the room.
    RoomInfo getInitialSync(String roomId);

    String sendStateEvent(String roomId, String eventType, String stateKey, RoomStateEvent<?> matrixRoomMessageEvent);

    /// Creates a `m.room.message` event to a Matrix room.
    ///
    /// @param roomId           the room ID where to send the event.
    /// @param roomMessageEvent a well constructed [RoomMessageEvent].
    /// @return a [String] representing a unique identifier of the event.
    /// @throws MatrixIOException      when the payload cannot be processed.
    /// @throws MatrixNetworkException when the response status is not successful.
    String sendMessageEvent(String roomId, RoomMessageEvent roomMessageEvent);

    /// Strips all information out of an event which isn’t critical to the integrity of the server-side representation of the room.
    ///
    /// **This cannot be undone.**
    ///
    /// If the server advertises support for sending a state event using `m.room.redact`,
    /// use [#sendStateEvent(String, String, String, RoomStateEvent)]
    ///
    /// @param roomId  the room ID where to redact the event.
    /// @param eventId the event ID of the event to target and redact.
    /// @param txnId   the transaction ID of the event.
    /// @param reason  the reason of the redaction.
    /// @return a [String] representing a unique identifier of the event.
    String redactEvent(String roomId, String eventId, String txnId, String reason);

    /// Synchronously uploads a local multimedia resource to the Matrix media server.
    ///
    /// @param resource the [Path] of the resource to upload.
    /// @return a [String] containing the MXC upon a successful upload.
    String uploadResource(Path resource);

    /// Sends a `/sync` request, this method is not responsible for any type of HTTP Polling.
    ///
    /// @param params the [QueryParametersSync] for the query, not all are required.
    /// @return [Sync] with all the corresponding information.
    /// @throws MatrixIOException      when the payload cannot be processed.
    /// @throws MatrixNetworkException when the response status is not successful.
    Sync sync(QueryParametersSync params);


}
