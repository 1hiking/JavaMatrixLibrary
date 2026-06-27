package org.hik.payloads.roomstate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public record RoomSummary(@JsonProperty("allowed_rooms_ids") String allowedRoomsIds,
                          @JsonProperty("avatar_url") URI avatarUrl,
                          @JsonProperty("room_alias") String roomAlias,
                          String encryption,
                          @JsonProperty("guest_can_join") boolean guestCanJoin,
                          @JsonProperty("join_rule") String joinRule,
                          String membership,
                          String name,
                          @JsonProperty("num_joined_members") Integer numJoinedMembers,
                          @JsonProperty("room_id") String roomId,
                          @JsonProperty("room_type") String roomType,
                          @JsonProperty("room_version") String roomVersion,
                          String topic,
                          @JsonProperty("world_readable") boolean worldReadable) {
}
