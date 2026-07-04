package org.hik.api.events;

/// Marks event content that includes thumbnail metadata such as E2E metadata
/// their width, size and height and url
/// `info` object.
///
/// @see MatrixFile.FileInfo
/// @see MatrixLocation.LocationInfo
/// @see MatrixImage.ImageInfo
/// @see MatrixVideo.VideoInfo
public sealed interface HasThumbnail
        permits MatrixFile.FileInfo, MatrixLocation.LocationInfo, MatrixImage.ImageInfo, MatrixVideo.VideoInfo {
    /// @return not implemented yet.
    EncryptedFile thumbnailFile();

    /// @return metadata about the resource referred to in thumbnail\_url
    ThumbnailInfo thumbnailInfo();

    /// @return the URL to the thumbnail of the resource. Only present if the thumbnail is unencrypted.
    String thumbnailUrl();
}
