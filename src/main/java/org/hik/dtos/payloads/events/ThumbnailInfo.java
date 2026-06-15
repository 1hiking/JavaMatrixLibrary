package org.hik.dtos.payloads.events;

public record ThumbnailInfo(Integer h,
                            String mimetype,
                            Integer size,
                            Integer w) {
}
