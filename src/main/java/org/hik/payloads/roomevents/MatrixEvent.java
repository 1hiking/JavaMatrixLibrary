package org.hik.payloads.roomevents;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/// The interface that all matrix m.room.events have to inherit.
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "msgtype",
        visible = true
)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public sealed interface MatrixEvent
        permits MatrixAudio, MatrixFile, MatrixLocation, MatrixImage, MatrixText, MatrixVideo {
    /// @return the event type represented with a "m." prefix.
    String msgtype();

    /// @return depending on the event it can either be an url mxc:// or a text to show.
    String body();

}

