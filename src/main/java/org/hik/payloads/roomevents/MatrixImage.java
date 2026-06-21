package org.hik.payloads.roomevents;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;


/// This event represents an image
///
/// @param body          the filename of the original upload if `filename` is unset
///                      or identical to it; otherwise, a caption for the image.
/// @param file          information on the encrypted file, as specified in End-to-end
///                      encryption. Required if the file is encrypted.
/// @param filename      the original filename of the uploaded file.
/// @param format        the format used in `formattedBody`. Required if
///                      `formattedBody` is specified; currently only
///                      `org.matrix.custom.html` is supported.
/// @param formattedBody the formatted version of `body`, when it acts as a caption.
///                      Required if `format` is specified.
/// @param info          metadata for the audio clip referred to by `url`.
/// @param url           required if the file is unencrypted.
public record MatrixImage(String body,
                          EncryptedFile file,
                          String filename,
                          String format,
                          @JsonProperty("formatted_body") String formattedBody,
                          ImageInfo info,
                          URI url

) implements MatrixEvent {

    @Override
    public String msgtype() {
        return "m.image";
    }


    /// Additional file information referred in the [MatrixFile] `url` field.
    ///
    /// @param h             the intended display height of the image in pixels. This may differ from the intrinsic dimensions of the image file.
    /// @param w             the intended display width of the image in pixels. This may differ from the intrinsic dimensions of the image file.
    /// @param isAnimated    when set to true, the image SHOULD be assumed to be animated. Leave unset if unable to determine.
    /// @param mimetype      the mimetype of the image.
    /// @param size          the size of the image in bytes.
    /// @param thumbnailFile information on the encrypted thumbnail file. Currently not supported.
    /// @param thumbnailInfo metadata about the image referred to in `thumbnailUrl`.
    /// @param thumbnailUrl  the URL to the thumbnail of the file. Only present if the thumbnail is unencrypted.
    public record ImageInfo(
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

