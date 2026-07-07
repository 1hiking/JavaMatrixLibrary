package org.hik.api.rooms;


public record JoinRoomRequest(String reason,
                              ThirdPartySigned thirdPartySigned) {
}
