package org.hik.api.rooms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/// Record used to store all the information about published rooms on the server.
///
/// @param chunk                  a paginated chunk of published rooms.
/// @param nextBatch              a pagination token for the response. If null, then the record contains data of the
///  last page
/// @param prevBatch              a pagination token that allows fetching previous results. If null, then this is the
///  first batch
/// @param totalRoomCountEstimate if available, an estimate on the total number of published rooms
public record PublicRoomDirectory(@JsonProperty List<PublishedRoomsChunk> chunk,
                                  String nextBatch,
                                  String prevBatch,
                                  Integer totalRoomCountEstimate) {
}
