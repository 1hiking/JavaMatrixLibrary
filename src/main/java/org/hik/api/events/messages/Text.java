package org.hik.api.events.messages;

import org.hik.api.events.RoomMessageEvent;

/// The most basic message type, used to represent plain or formatted text.
///
/// @param body          the plain-text body of the message
/// @param format        the format used for formattedBody, e.g. "org.matrix.custom.html" (optional)
/// @param formattedBody the HTML-formatted version of the body (optional)
public record Text(
        String body,
        String format,
        String formattedBody
) implements RoomMessageEvent {

    @Override
    public String msgtype() {
        return "m.text";
    }

}
