package io.github.hikingc.matrixsdk.api.events.model;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;
import io.github.hikingc.matrixsdk.api.events.content.RoomTopic;

public record RoomTopicEvent(
    RoomTopic content,
    String eventId,
    Long originServerTs,
    String roomId,
    String sender,
    UnsignedData unsigned)
    implements SingletonStateEvent<RoomTopic> {

  @Override
  public String type() {
    return "m.room.topic";
  }
}
