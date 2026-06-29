package org.hik.payloads.roomevents;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/// The interface that all matrix m.room.events have to inherit.
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public sealed interface MatrixEvent
        permits MatrixAudio, MatrixFile, MatrixLocation, MatrixImage, MatrixText, MatrixVideo {

    /// @return the event type represented with a "m." prefix.
    @JsonProperty("msgtype")
    // required or it will break serialization
    String msgtype();

    /// @return depending on the event it can either be an url mxc:// or a text to show.
    String body();

}

