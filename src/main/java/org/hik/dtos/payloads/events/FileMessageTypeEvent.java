package org.hik.dtos.payloads.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FileMessageTypeEvent(
         String msgtype,
         String body,
         String url,
         ImageInfo info
) implements MessageTypeEvent {

    public record ImageInfo(
            @JsonProperty("h") Integer h,
            @JsonProperty("w") Integer w,
            @JsonProperty("mimetype") String mimetype,
            @JsonProperty("size") Long size) {}
}