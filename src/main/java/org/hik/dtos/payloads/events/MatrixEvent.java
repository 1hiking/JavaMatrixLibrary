package org.hik.dtos.payloads.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The interface that all matrix m.room.events have to inherit.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "msgtype",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MatrixText.class, name = MatrixText.TYPE),
        @JsonSubTypes.Type(value = MatrixImage.class, name = MatrixImage.TYPE),
        @JsonSubTypes.Type(value = MatrixFile.class, name = MatrixFile.TYPE),
        @JsonSubTypes.Type(value = MatrixAudio.class, name = MatrixAudio.TYPE),
        @JsonSubTypes.Type(value = MatrixVideo.class, name = MatrixVideo.TYPE),
        @JsonSubTypes.Type(value = MatrixLocation.class, name = MatrixLocation.TYPE),
})
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public sealed interface MatrixEvent
        permits MatrixAudio, MatrixFile, MatrixLocation, MatrixImage, MatrixText, MatrixVideo {
    /**
     * @return The event type represented with a "m." prefix
     */
    String msgtype();

    /**
     * @return Depending on the event it can either be an url mxc:// or a text to show.
     */
    String body();

}

