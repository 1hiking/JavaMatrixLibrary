package io.github.hikingc.matrixsdk.api.events.content;

public record RoomJoinRules(AllowCondition allow, String joinRule) implements StateEventContent {

  public record AllowCondition(String roomId, String type) {}
}
