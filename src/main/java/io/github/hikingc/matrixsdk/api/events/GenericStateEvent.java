package io.github.hikingc.matrixsdk.api.events;

/// Holds data of a state event in a room, this record is implemented in a way that it does **NOT**
/// deserialize the `content` fields and leaves it up to application developers.
///
/// @param content        the content of a Matrix event
/// @param eventId        globally unique identifier of the event
/// @param originServerTs a timestamp in milliseconds on originating homeserver when this event was sent.
/// @param roomId         the ID of the room associated with this event. Will not be present on events that arrive through `/sync`, despite being required everywhere else.
/// @param sender         contains the fully-qualified ID of the user who sent this event.
/// @param userId         the ID of whoever sent this event
/// @param stateKey       A unique key which defines the overwriting semantics for this piece of room state. This value is often a zero-length string.
///
/// The presence of this key makes this event a State Event. State keys starting with an @ are reserved for referencing user IDs, such as room members.
///
/// Except a few events, state events set with a given user’s ID as the state key **MUST** only be set by that user.
/// @param type           the type of the event
/// @param unsigned       it contains optional extra information about the event. By specification, it may only set `age` value for this record.
public record GenericStateEvent(Object content,
                                String eventId,
                                Long originServerTs,
                                String roomId,
                                String sender,
                                String userId,
                                String stateKey,
                                String type,
                                UnsignedData unsigned) implements RoomStateEvent<Object> {
}
