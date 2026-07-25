package org.hik.api.events.messages;

/// Marks event content that includes file metadata such as a MIME type
/// and size in bytes, as described by the Matrix specification's
/// `info` object.
///
/// @see Audio.AudioInfo
/// @see Image.ImageInfo
/// @see File.FileInfo
/// @see Video.VideoInfo
public sealed interface HasInfo permits Audio.AudioInfo, File.FileInfo, Image.ImageInfo,
        Video.VideoInfo {
    /// @return the mimetype of the corresponding input resource
    String mimetype();


    /// @return the size of the input resource in bytes.
    Integer size();
}

