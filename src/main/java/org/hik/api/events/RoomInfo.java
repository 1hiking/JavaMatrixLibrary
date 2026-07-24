package org.hik.api.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hik.api.Room;
import org.hik.api.rooms.PublicRoomRequest;

import java.util.List;

/// Represents the current state of a room
///
/// @param accountData the private data that this user has attached to this room.
/// @param membership  the user’s membership state in this room. One of `[invite, join, leave, ban]`
/// @param messages    the pagination chunk for this room.
/// @param roomId      the ID of this room.
/// @param state       if the user is a member of the room this will be the current state of the room as a list of events.
/// **If the user has left the room this will be the state of the room when they left it.**
/// @param visibility  If it's visible in either [Room#getPublishedRoomDirectory(Integer, String, String)]
/// or [Room#getPublishedRoomDirectory(PublicRoomRequest)]
public record RoomInfo(List<Event> accountData,
                       String membership,
                       PaginationChunk messages,
                       @JsonProperty(required = true) String roomId,
                       List<ClientEvent> state,
                       String visibility) {

    /// Holds a Matrix event, which can then be serialized as one of either [RoomStateEvent] or [RoomMessageEvent] for example.
    ///
    /// @param content the fields of an event.
    /// @param type    the type of the event, for example, `m.tag`
    /// @see <a href="https://spec.matrix.org/v1.19/client-server-api/#types-of-room-events">Types of room events in the Matrix specification.</a>
    public record Event(@JsonProperty(required = true) Object content,
                        @JsonProperty(required = true) String type) {
    }

    /// Holds pagination metadata.
    ///
    /// @param chunk if the user is a member of the room this will be a [List] of the most recent messages for this room, otherwise
    /// if the user has left the room then the messages that preceded them before leaving. This array will consist of at most `limit` elements.
    /// @param end   a token which correlates to the end of `chunk`.
    /// Can be used in [org.hik.api.Event#getMessages(String, ChronologicalDirection, QueryParametersMessages)] to retrieve later events.
    /// @param start a token which correlates to the start of `chunk`.
    /// Can be used in [org.hik.api.Event#getMessages(String, ChronologicalDirection, QueryParametersMessages)] to retrieve earlier events.
    public record PaginationChunk(@JsonProperty(required = true) List<ClientEvent> chunk,
                                  @JsonProperty(required = true) String end,
                                  String start) {

    }


}
