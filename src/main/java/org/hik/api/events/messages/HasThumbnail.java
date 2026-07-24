package org.hik.api.events.messages;

import org.hik.api.events.EncryptedFile;

/// Marks event content that includes thumbnail metadata such as E2E metadata
/// their width, size and height and url
/// `info` object.
///
/// @see File.FileInfo
/// @see Location.LocationInfo
/// @see Image.ImageInfo
/// @see Video.VideoInfo
public sealed interface HasThumbnail
        permits File.FileInfo, Location.LocationInfo, Image.ImageInfo, Video.VideoInfo {
    /// @return not implemented yet.
    EncryptedFile thumbnailFile();

    /// @return metadata about the resource referred to in thumbnail\_url
    ThumbnailInfo thumbnailInfo();

    /// @return the URL to the thumbnail of the resource. Only present if the thumbnail is unencrypted.
    String thumbnailUrl();
}
