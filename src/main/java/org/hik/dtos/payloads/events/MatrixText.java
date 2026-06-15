package org.hik.dtos.payloads.events;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The most basic message type, used to represent plain or formatted text.
 *
 * @param msgtype       always "m.text" for this message type
 * @param body          the plain-text body of the message
 * @param format        the format used for formattedBody, e.g. "org.matrix.custom.html" (optional)
 * @param formattedBody the HTML-formatted version of the body (optional)
 */
public record MatrixText(
        String msgtype,
        String body,
        String format,
        @JsonProperty("formatted_body") String formattedBody
) implements MatrixEvent {

    public static final String TYPE = "m.text";

    public MatrixText(String body, String format, String formattedBody) {
        this(TYPE, body, format, formattedBody);
    }

    public MatrixText(String body) {
        this(TYPE, body, null, null);
    }
}
