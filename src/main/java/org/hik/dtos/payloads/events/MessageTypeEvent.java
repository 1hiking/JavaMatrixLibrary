package org.hik.dtos.payloads.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "msgtype",
        visible = true
)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public sealed interface MessageTypeEvent
        permits TextMessageTypeEvent, FileMessageTypeEvent {
    String msgtype();

    String body();
}

