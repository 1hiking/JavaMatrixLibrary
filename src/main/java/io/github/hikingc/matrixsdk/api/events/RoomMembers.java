package io.github.hikingc.matrixsdk.api.events;

import java.net.URI;
import java.util.Map;

public record RoomMembers(Map<String, RoomMember> joined) {

  public record RoomMember(URI avatarUrl, String displayName) {}
}
