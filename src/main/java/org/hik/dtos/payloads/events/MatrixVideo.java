package org.hik.dtos.payloads.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public record MatrixVideo(String body,
                          EncryptedFile file,
                          String filename,
                          String format,
                          @JsonProperty("formatted_body") String formattedBody,
                          VideoInfo info,
                          String msgtype,
                          URI url
) implements MatrixEvent {

    public static final String TYPE = "m.video";

    public MatrixVideo(String body, String filename, URI url) {
        this(body, null, filename, null, null, null, TYPE, url);
    }


    record VideoInfo(Integer duration,
                     Integer h,
                     String  mimetype,
                     Integer size,
                     @JsonProperty("thumbnail_file") EncryptedFile thumbnailFile,
                     @JsonProperty("thumbnail_info") ThumbnailInfo thumbnailInfo,
                     @JsonProperty("thumbnail_url") String thumbnailUrl,
                     Integer w
                     ) implements HasThumbnail, HasInfo {
    }
}
