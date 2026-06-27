package org.hik.payloads.roomstate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PublicRoomDirectory(@JsonProperty List<PublishedRoomsChunk> chunk,
                                  @JsonProperty("next_batch") String nextBatch,
                                  @JsonProperty("prev_batch") String prevBatch,
                                  @JsonProperty("total_room_count_estimate") Integer totalRoomCountEstimate) {
}
