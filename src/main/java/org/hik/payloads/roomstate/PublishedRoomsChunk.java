package org.hik.payloads.roomstate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public record PublishedRoomsChunk(@JsonProperty("avatar_url") URI avatarUrl,
                                  @JsonProperty("canonical_alias") String canonicalAlias,
                                  @JsonProperty("guest_can_join") boolean guestCanJoin,
                                  @JsonProperty("join_rule") String joinRule,
                                  String name,
                                  @JsonProperty("num_joined_members") int numJoinedMembers,
                                  @JsonProperty("room_id") String roomId,
                                  @JsonProperty("room_type") String roomType,
                                  String topic,
                                  @JsonProperty("world_readable") boolean worldReadable) {}
