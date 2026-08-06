package io.github.hikingc.matrixsdk.api.events.model;

import io.github.hikingc.matrixsdk.api.events.StateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;
import io.github.hikingc.matrixsdk.api.events.content.RoomMember;

public record RoomMemberEvent(
    RoomMember content,
    String eventId,
    Long originServerTs,
    String roomId,
    String sender,
    String stateKey,
    UnsignedData unsigned)
    implements StateEvent<RoomMember> {

  @Override
  public String type() {
    return "m.room.member";
  }
}
