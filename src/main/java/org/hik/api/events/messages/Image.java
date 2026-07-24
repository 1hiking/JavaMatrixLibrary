package org.hik.api.events.messages;

import org.hik.api.events.EncryptedFile;
import org.hik.api.events.RoomMessageEvent;

import java.net.URI;


/// This type of message represents an image
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
public record Image(String body,
                    EncryptedFile file,
                    String filename,
                    String format,
                    String formattedBody,
                    ImageInfo info,
                    URI url

) implements RoomMessageEvent {

    @Override
    public String msgtype() {
        return "m.image";
    }


    /// Additional file information referred in the [File] `url` field.
    ///
    /// @param h             the intended display height of the image in pixels. This may differ from the intrinsic
    /// dimensions of the image file.
    /// @param w             the intended display width of the image in pixels. This may differ from the intrinsic
    /// dimensions of the image file.
    /// @param isAnimated    when set to true, the image SHOULD be assumed to be animated. Leave unset if unable to
    /// determine.
    /// @param mimetype      the mimetype of the image.
    /// @param size          the size of the image in bytes.
    /// @param thumbnailFile information on the encrypted thumbnail file. Currently not supported.
    /// @param thumbnailInfo metadata about the image referred to in `thumbnailUrl`.
    /// @param thumbnailUrl  the URL to the thumbnail of the file. Only present if the thumbnail is unencrypted.
    public record ImageInfo(
            Integer h,
            Integer w,
            Boolean isAnimated,
            String mimetype,
            Integer size,
            EncryptedFile thumbnailFile,
            ThumbnailInfo thumbnailInfo,
            String thumbnailUrl
    ) implements HasInfo, HasThumbnail {

    }


}

