package io.github.hikingc.matrixsdk.api.rooms.queries;

import com.fasterxml.jackson.annotation.JsonProperty;

/// The visibility type for a room.
public enum VisibilityRoomType {
  /// Set the visibility private.
  @JsonProperty("private")
  PRIVATE,
  /// Set the visibility to public.
  @JsonProperty("public")
  PUBLIC
}
