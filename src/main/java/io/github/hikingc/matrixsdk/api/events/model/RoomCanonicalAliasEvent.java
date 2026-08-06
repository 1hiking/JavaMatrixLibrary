package io.github.hikingc.matrixsdk.api.events.model;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;
import io.github.hikingc.matrixsdk.api.events.content.RoomCanonicalAlias;

public record RoomCanonicalAliasEvent(
    RoomCanonicalAlias content,
    String eventId,
    Long originServerTs,
    String roomId,
    String sender,
    UnsignedData unsigned)
    implements SingletonStateEvent<RoomCanonicalAlias> {

  @Override
  public String type() {
    return "m.room.canonical_alias";
  }
}
