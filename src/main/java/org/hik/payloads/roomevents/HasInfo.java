package org.hik.payloads.roomevents;

/// Marks event content that includes file metadata such as a MIME type
/// and size in bytes, as described by the Matrix specification's
/// `info` object.
///
/// @see MatrixAudio.AudioInfo
/// @see MatrixImage.ImageInfo
/// @see MatrixFile.FileInfo
/// @see MatrixVideo.VideoInfo
public sealed interface HasInfo permits MatrixAudio.AudioInfo, MatrixFile.FileInfo, MatrixImage.ImageInfo, MatrixVideo.VideoInfo {
    /// @return the mimetype of the corresponding input resource
    String mimetype();


    /// @return the size of the input resource in bytes.
    Integer size();
}

