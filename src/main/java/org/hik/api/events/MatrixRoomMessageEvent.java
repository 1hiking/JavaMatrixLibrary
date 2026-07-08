package org.hik.api.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/// The interface that all matrix m.room.events have to inherit.
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public sealed interface MatrixRoomMessageEvent
        permits MatrixAudio, MatrixFile, MatrixLocation, MatrixImage, MatrixText, MatrixVideo {

    /// Message type constant field required by all messages.
    ///
    /// @return the event type represented with a "m." prefix.
    @JsonProperty("msgtype")
    String msgtype();

    /// The body field that all messages require.
    ///
    /// @return depending on the event it can either be an url mxc:// or a text to show.
    String body();

}

