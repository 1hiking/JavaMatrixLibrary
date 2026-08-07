package io.github.hikingc.matrixsdk.api.events.model;

import io.github.hikingc.matrixsdk.api.events.MessageEvent;
import io.github.hikingc.matrixsdk.api.events.content.RoomMessage;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

/// Represents all the types `m.room.message` events from the server.
public record RoomMessageEvent(
    RoomMessage content,
    String eventId,
    Long originServerTs,
    String roomId,
    String sender,
    UnsignedData unsigned)
    implements MessageEvent<RoomMessage> {

  /// @return the type of the event.
  @Override
  public String type() {
    return "m.room.message";
  }
}
