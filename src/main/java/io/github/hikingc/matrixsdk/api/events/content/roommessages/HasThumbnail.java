package io.github.hikingc.matrixsdk.api.events.content.roommessages;

import io.github.hikingc.matrixsdk.api.events.crypto.EncryptedFile;

/// Marks event content that includes thumbnail metadata such as E2E metadata their width, size and
/// height and url `info` object.
///
/// @see FileContent.FileInfo
/// @see LocationContent.LocationInfo
/// @see ImageContent.ImageInfo
/// @see VideoContent.VideoInfo
public sealed interface HasThumbnail
    permits FileContent.FileInfo,
        LocationContent.LocationInfo,
        ImageContent.ImageInfo,
        VideoContent.VideoInfo {
  /// @return not implemented yet.
  EncryptedFile thumbnailFile();

  /// @return metadata about the resource referred to in thumbnail\_url
  ThumbnailInfo thumbnailInfo();

  /// @return the URL to the thumbnail of the resource. Only present if the thumbnail is
  ///   unencrypted.
  String thumbnailUrl();
}
