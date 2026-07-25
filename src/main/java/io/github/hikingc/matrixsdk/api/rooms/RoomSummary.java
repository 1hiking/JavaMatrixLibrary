package io.github.hikingc.matrixsdk.api.rooms;

import java.net.URI;

public record RoomSummary(String allowedRoomsIds,
                          URI avatarUrl,
                          String roomAlias,
                          String encryption,
                          boolean guestCanJoin,
                          String joinRule,
                          String membership,
                          String name,
                          Integer numJoinedMembers,
                          String roomId,
                          String roomType,
                          String roomVersion,
                          String topic,
                          boolean worldReadable) {
}
