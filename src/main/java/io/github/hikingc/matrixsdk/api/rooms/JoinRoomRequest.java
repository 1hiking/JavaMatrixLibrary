package io.github.hikingc.matrixsdk.api.rooms;

/// Holds optional information to supply when attempting to join a room.
///
/// @param reason of the membership request
/// @param thirdPartySigned if supplied, the homeserver must verify that it matches a pending `m.room.third_party_invite` event in the room, and perform key validity checking if required by the event.
public record JoinRoomRequest(String reason,
                              ThirdPartySigned thirdPartySigned) {
}
