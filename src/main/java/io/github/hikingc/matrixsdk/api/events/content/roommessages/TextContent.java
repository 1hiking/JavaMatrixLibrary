package io.github.hikingc.matrixsdk.api.events.content.roommessages;

import io.github.hikingc.matrixsdk.api.events.RoomMessage;

/// The most basic message type, used to represent plain or formatted text.
///
/// @param body the plain-text body of the message
/// @param format the format used for formattedBody, e.g. "org.matrix.custom.html" (optional)
/// @param formattedBody the HTML-formatted version of the body (optional)
public record TextContent(String body, String format, String formattedBody) implements RoomMessage {

  @Override
  public String msgtype() {
    return "m.text";
  }
}
