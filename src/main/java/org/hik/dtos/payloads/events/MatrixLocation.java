package org.hik.dtos.payloads.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MatrixLocation(String body,
                             @JsonProperty("geo_uri") String geoUrI,
                             LocationInfo info,
                             String msgtype
) implements MatrixEvent {

    public static final String TYPE = "m.location";


    record LocationInfo(EncryptedFile thumbnailFile,
                        ThumbnailInfo thumbnailInfo,
                        String thumbnailUrl
    ) implements HasThumbnail {}
}

