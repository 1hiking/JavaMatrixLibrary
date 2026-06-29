package org.hik.payloads.roomevents;

/// This record is used by various events to store metadata information
///
/// @param h        the intended display height of the image in pixels. This may differ from the intrinsic dimensions
///  of the image file.
/// @param w        the intended display width of the image in pixels. This may differ from the intrinsic dimensions
/// of the image file.
/// @param mimetype the mimetype of the image.
/// @param size     the size of the image in bytes.
public record ThumbnailInfo(Integer h,
                            String mimetype,
                            Integer size,
                            Integer w) {
}
