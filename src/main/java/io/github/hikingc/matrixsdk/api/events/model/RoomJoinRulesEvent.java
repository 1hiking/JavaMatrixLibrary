package io.github.hikingc.matrixsdk.api.events.model;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;
import io.github.hikingc.matrixsdk.api.events.content.RoomJoinRules;

public record RoomJoinRulesEvent(
    RoomJoinRules content,
    String eventId,
    Long originServerTs,
    String roomId,
    String sender,
    UnsignedData unsigned)
    implements SingletonStateEvent<RoomJoinRules> {

  @Override
  public String type() {
    return "m.room.join_rules";
  }
}
