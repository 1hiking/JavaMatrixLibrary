package io.github.hikingc.matrixsdk.api.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hikingc.matrixsdk.api.events.content.roommessages.*;

/// Interface that enforces fields required by all `m.room.message` content events.
public sealed interface RoomMessage
    permits AudioContent, FileContent, LocationContent, ImageContent, TextContent, VideoContent {

  /// Message type constant field required by all types of messages.
  ///
  /// @return the event type represented with a "m." prefix.
  @JsonProperty("msgtype")
  String msgtype();

  /// The body field that all types of messages require.
  ///
  /// @return depending on the event it can either be an url mxc:// or a text to show.
  String body();
}
