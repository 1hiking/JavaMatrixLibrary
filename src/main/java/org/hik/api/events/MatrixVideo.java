package org.hik.api.events;

import java.net.URI;

/// This type of message represents a single video clip.
///
/// @param body          the filename of the original upload if `filename` is unset
///                      or identical to it; otherwise, a caption for the video.
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
public record MatrixVideo(String body,
                          EncryptedFile file,
                          String filename,
                          String format,
                          String formattedBody,
                          VideoInfo info,
                          URI url
) implements MatrixRoomMessageEvent {


    @Override
    public String msgtype() {
        return "m.video";
    }


    /// Additional file information referred in the [MatrixAudio] `url` field.
    ///
    /// @param h             the height of the video in pixels.
    /// @param w             The width of the video in pixels.
    /// @param mimetype      the mimetype of the image.
    /// @param size          the size of the image in bytes.
    /// @param thumbnailFile information on the encrypted thumbnail file. Currently not supported.
    /// @param thumbnailInfo metadata about the image referred to in `thumbnailUrl`.
    /// @param thumbnailUrl  the URL to the thumbnail of the file. Only present if the thumbnail is unencrypted.
    /// @param duration      the duration of the video in milliseconds.
    public record VideoInfo(Integer duration,
                            Integer h,
                            String mimetype,
                            Integer size,
                            EncryptedFile thumbnailFile,
                            ThumbnailInfo thumbnailInfo,
                            String thumbnailUrl,
                            Integer w
    ) implements HasThumbnail, HasInfo {
    }
}
