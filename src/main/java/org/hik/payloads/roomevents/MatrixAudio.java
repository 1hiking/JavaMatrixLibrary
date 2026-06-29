package org.hik.payloads.roomevents;

import java.net.URI;

/// This event represents a single audio clip.
///
/// @param body          the filename of the original upload if `filename` is unset
///                      or identical to it; otherwise, a caption for the audio.
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
public record MatrixAudio(String body,
                          EncryptedFile file,
                          String filename,
                          String format,
                          String formattedBody,
                          AudioInfo info,
                          URI url) implements MatrixEvent {


    @Override
    public String msgtype() {
        return "m.audio";
    }


    /// Metadata describing an audio clip.
    ///
    /// @param duration the duration of the audio in milliseconds.
    /// @param mimetype the mimetype of the audio.
    /// @param size     the size of the audio in bytes.
    public record AudioInfo(Integer duration,
                            String mimetype,
                            Integer size
    ) implements HasInfo {
    }
}