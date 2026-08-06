package io.github.hikingc.matrixsdk.api.events.model;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;
import io.github.hikingc.matrixsdk.api.events.content.RoomPowerLevels;

public record RoomPowerLevelsEvent(
    RoomPowerLevels content,
    String eventId,
    Long originServerTs,
    String roomId,
    String sender,
    UnsignedData unsigned)
    implements SingletonStateEvent<RoomPowerLevels> {

  @Override
  public String type() {
    return "m.room.power_levels";
  }
}
