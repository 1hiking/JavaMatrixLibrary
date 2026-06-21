package org.hik.payloads.roomevents;

import com.fasterxml.jackson.annotation.JsonProperty;

/// The most basic message type, used to represent plain or formatted text.
///
/// @param body          the plain-text body of the message
/// @param format        the format used for formattedBody, e.g. "org.matrix.custom.html" (optional)
/// @param formattedBody the HTML-formatted version of the body (optional)
public record MatrixText(
        String body,
        String format,
        @JsonProperty("formatted_body") String formattedBody
) implements MatrixEvent {

    @Override
    public String msgtype() {
        return "m.text";
    }

}
