package org.hik.api.events;


import java.util.List;

/// Represents a list of messages and state events from a room.
///
/// @param start a token corresponding to the start of chunk. This will be the same as the value given in `from`.
/// @param end   a token corresponding to the end of chunk. This token can be passed back to this endpoint to request
///  further events.
/// @param chunk a list of room events. This list will be paginated according to the [ChronologicalDirection] used in the request.
/// @param state a list of state events relevant to showing the chunk. For example, if lazy_load_members is enabled in
///  the filter then this may contain the membership events for the senders of events in the chunk.
public record Messages(String start,
                       String end,
                       List<ClientEvent> chunk,
                       List<ClientEvent> state) {

}
