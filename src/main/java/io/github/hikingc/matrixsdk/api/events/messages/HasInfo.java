package io.github.hikingc.matrixsdk.api.events.messages;

import io.github.hikingc.matrixsdk.api.events.states.RoomAvatar;

/// Marks event content that includes file metadata such as a MIME type
/// and size in bytes, as described by the Matrix specification's
/// `info` object.
///
/// @see Audio.AudioInfo
/// @see Image.ImageInfo
/// @see File.FileInfo
/// @see Video.VideoInfo
public sealed interface HasInfo permits Audio.AudioInfo, File.FileInfo, Image.ImageInfo, Video.VideoInfo, RoomAvatar.AvatarInfo {
    /// @return the mimetype of the corresponding input resource
    String mimetype();


    /// @return the size of the input resource in bytes.
    Integer size();
}

