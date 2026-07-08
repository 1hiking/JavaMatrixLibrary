package org.hik.api.events;

/// This type of message represents a real-world location.
///
/// @param body   the filename of the original upload if `filename` is unset
///               or identical to it; otherwise, a caption for the image.
/// @param info   metadata for the audio clip referred to by `url`.
/// @param geoUri A geo URI (RFC5870) representing this location.
public record MatrixLocation(String body,
                             String geoUri,
                             LocationInfo info
) implements MatrixRoomMessageEvent {


    @Override
    public String msgtype() {
        return "m.location";
    }


    /// Additional information of the location data
    ///
    /// @param thumbnailFile information on the encrypted thumbnail file. Currently not supported.
    /// @param thumbnailInfo metadata about the image referred to in `thumbnailUrl`.
    /// @param thumbnailUrl  the URL to the thumbnail of the file. Only present if the thumbnail is unencrypted.
    public record LocationInfo(EncryptedFile thumbnailFile,
                               ThumbnailInfo thumbnailInfo,
                               String thumbnailUrl
    ) implements HasThumbnail {
    }
}

