package org.hik.responses;


import java.util.List;

/// This is the record that is returned as a list of message and state events for a room.
/// It is paginated if queried as such with [org.hik.payloads.roomstate.ChronologicalDirectionEvent].
///
/// @param start A token corresponding to the start of chunk. This will be the same as the value given in from.
/// @param end   A token corresponding to the end of chunk. This token can be passed back to this endpoint to request further events.
/// @param chunk A list of room events.
public record MessagesResponse(String start,
                               String end,
                               List<ClientEvent> chunk) {

}
