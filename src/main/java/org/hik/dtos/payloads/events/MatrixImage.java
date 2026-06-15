package org.hik.dtos.payloads.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;


/**
 * @param body
 * @param file
 * @param filename
 * @param format
 * @param formattedBody
 * @param info
 * @param msgtype
 * @param url
 */
public record MatrixImage(String body,
                          EncryptedFile file,
                          String filename,
                          String format,
                          @JsonProperty("formatted_body") String formattedBody,
                          ImageInfo info,
                          String msgtype,
                          URI url

) implements MatrixEvent {
    public static final String TYPE = "m.text";

    /**
     * @param body
     * @param filename
     * @param url
     */
    public MatrixImage(String body, String filename, URI url) {
        this(body, null, filename, null, null, null, TYPE, url);
    }


    record ImageInfo(
            Integer h,
            Integer w,
            @JsonProperty("is_animated") Boolean isAnimated,
            String mimetype,
            Integer size,
            @JsonProperty("thumbnail_file") EncryptedFile thumbnailFile,
            @JsonProperty("thumbnail_info") ThumbnailInfo thumbnailInfo,
            @JsonProperty("thumbnail_url") String thumbnailUrl
    ) implements HasInfo, HasThumbnail {
    }


}

