package org.hik.payloads.roomstate;

import java.util.List;

/// This record contains data when resolving a room alias.
///
/// @param roomId  the room id for the room alias.
/// @param servers a list of servers aware of said alias.
///
public record RoomAliasData(String roomId,
                            List<String> servers) {
}
