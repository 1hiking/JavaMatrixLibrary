package org.hik.api.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hik.api.events.messages.*;

/// Interface that enforces fields required by all `m.room.message` events.
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public sealed interface RoomMessageEvent
        permits Audio, File, Location, Image, Text, Video {

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

