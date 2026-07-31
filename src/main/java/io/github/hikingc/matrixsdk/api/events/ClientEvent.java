package io.github.hikingc.matrixsdk.api.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


/// Represents the format used for event retrieval.
/// This record holds `content` in a [Map] and does **NOT** make any assumptions over what deserialized type it might be.
///
/// @param content        the body of this event, as created by the user which sent it.
/// @param eventId        the globally unique identifier for this event.
/// @param originServerTs timestamp (in milliseconds since the Unix epoch) on originating homeserver when this event
/// was sent.
/// @param roomId         the ID of the room associated with this event.
/// @param sender         contains the fully-qualified ID of the user who sent this event.
/// @param stateKey       present if, and only if, this event is a state event. The key making this piece of state
/// unique in the room. Note that it is often an empty string.
///
/// State keys starting with an @ are reserved for referencing user IDs, such as room members. Except a few events,
/// state events set with a given user’s ID as the state key MUST only be set by that user.
/// @param type           the type of the event.
/// @param unsigned       contains optional extra information about the event.
/// @see <a href="https://spec.matrix.org/latest/client-server-api/#room-event-format">The room event format as defined in the specification.</a>
public record ClientEvent(@JsonProperty(required = true) Map<String, Object> content,
                          @JsonProperty(required = true) String eventId,
                          @JsonProperty(required = true) Long originServerTs,
                          @JsonProperty(required = true) String roomId,
                          @JsonProperty(required = true) String sender,
                          String stateKey,
                          @JsonProperty(required = true) String type,
                          UnsignedData unsigned
) implements RoomStateEvent<Map<String, Object>> {
}
