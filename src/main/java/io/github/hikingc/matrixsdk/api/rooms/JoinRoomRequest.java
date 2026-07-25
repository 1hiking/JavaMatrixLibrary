package io.github.hikingc.matrixsdk.api.rooms;


public record JoinRoomRequest(String reason,
                              ThirdPartySigned thirdPartySigned) {
}
