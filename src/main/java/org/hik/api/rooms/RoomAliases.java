package org.hik.api.rooms;

import java.util.List;

/// Record that stores server’s local aliases on the room. Can be empty.
///
/// @param aliases the list of aliases
public record RoomAliases(List<String> aliases) {
}
