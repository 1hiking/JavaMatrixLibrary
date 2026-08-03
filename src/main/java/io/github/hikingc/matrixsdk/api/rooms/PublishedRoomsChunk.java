package io.github.hikingc.matrixsdk.api.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hikingc.matrixsdk.api.Room;
import io.github.hikingc.matrixsdk.api.identifiers.Validator;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.util.List;

/// Holds information about a room in specific.
///
/// @param avatarUrl        the [URI] for the room’s avatar, if one is set.
/// @param canonicalAlias   the canonical alias of the room, if any.
/// @param guestCanJoin     whether guest users may join the room and participate in it. If they can, they will be subject to ordinary power level rules like any other user.
/// @param joinRule         the room’s join rule. When not present, the room is assumed to be public.
/// @param name             the name of the room, if any.
/// @param numJoinedMembers the number of members joined to the room.
/// @param roomId           the ID of the room.
/// @param roomType         the type of room (from `m.room.create`), if any.
/// @param topic            the plain text topic of the room. Omitted if no text/plain mimetype exists in `m.room.topic`.
/// @param worldReadable    whether the room may be viewed by users without joining.
/// @see Room#getRoomSummary(Validator, List) getRoomSummary(), which returns an endpoint with additional values for a determinate room
public record PublishedRoomsChunk(URI avatarUrl,
                                  String canonicalAlias,
                                  @JsonProperty(required = true) boolean guestCanJoin,
                                  String joinRule,
                                  String name,
                                  @JsonProperty(required = true) int numJoinedMembers,
                                  @NonNull @JsonProperty(required = true) String roomId,
                                  String roomType,
                                  String topic,
                                  @JsonProperty(required = true) boolean worldReadable) {
}
