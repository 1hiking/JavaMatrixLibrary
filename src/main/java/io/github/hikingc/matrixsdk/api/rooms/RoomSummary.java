package io.github.hikingc.matrixsdk.api.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;

import java.net.URI;

/// Holds detailed information about a room in specific.
///
/// @param allowedRoomsIds
/// @param avatarUrl        the [URI] for the room’s avatar, if one is set.
/// @param canonicalAlias   the canonical alias of the room, if any.
/// @param encryption
/// @param guestCanJoin     whether guest users may join the room and participate in it. If they can, they will be subject to ordinary power level rules like any other user.
/// @param joinRule         the room’s join rule. When not present, the room is assumed to be public.
/// @param membership       the membership state of the user if the user is joined to the room. Absent if the API was called unauthenticated.
/// @param name             the name of the room, if any.
/// @param numJoinedMembers the number of members joined to the room.
/// @param roomId           the ID of the room.
/// @param roomType         the type of room (from `m.room.create`), if any.
/// @param roomVersion      the version of the room.
/// @param topic            the plain text topic of the room. Omitted if no text/plain mimetype exists in `m.room.topic`.
/// @param worldReadable    whether the room may be viewed by users without joining.
public record RoomSummary(String allowedRoomsIds,
                          URI avatarUrl,
                          String canonicalAlias,
                          String encryption,
                          boolean guestCanJoin,
                          String joinRule,
                          String membership, // enum One of: [invite, join, knock, leave, ban].
                          String name,
                          int numJoinedMembers,
                          @NonNull @JsonProperty(required = true) String roomId,
                          String roomType,
                          String roomVersion,
                          String topic,
                          boolean worldReadable) {
}
