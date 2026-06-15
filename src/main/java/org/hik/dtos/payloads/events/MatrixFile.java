package org.hik.dtos.payloads.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;



public record MatrixFile(String body,
                         EncryptedFile file,
                         String filename,
                         String format,
                         @JsonProperty("formatted_body") String formattedBody,
                         FileInfo info,
                         String msgtype,
                         URI url)
        implements MatrixEvent {
    public static final String TYPE = "m.file";


    public MatrixFile(String body, String filename, URI url) {
        this(body, null, filename, null, null, null, TYPE, url);
    }


    record FileInfo(
            String mimetype,
            Integer size,
            @JsonProperty("thumbnail_file") EncryptedFile thumbnailFile,
            @JsonProperty("thumbnail_info") ThumbnailInfo thumbnailInfo,
            @JsonProperty("thumbnail_url") String thumbnailUrl
    ) implements HasInfo, HasThumbnail {
    }
}
