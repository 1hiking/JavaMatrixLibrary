package org.hik.api.rooms;

import java.net.URI;

public record PublishedRoomsChunk(URI avatarUrl,
                                  String canonicalAlias,
                                  boolean guestCanJoin,
                                  String joinRule,
                                  String name,
                                  int numJoinedMembers,
                                  String roomId,
                                  String roomType,
                                  String topic,
                                  boolean worldReadable) {
}
