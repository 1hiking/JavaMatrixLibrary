package io.github.hikingc.matrixsdk.api.filters;

import java.util.List;

/// Holds all data points that can be used to restrict which rooms are returned to the client.
///
/// @param accountData the per-user account data to include for rooms.
/// @param ephemeral the ephemeral events to include for rooms. These are the events that appear in
///   the ephemeral property in the `/sync` response.
/// @param includeLeave what rooms to include that the user has left in the sync. Defaults to
///   `false`.
/// @param notRooms a list of room IDs to exclude. If this list is absent then no rooms are
///   excluded. A matching room will be excluded even if it is listed in the `rooms` filter. This
///   filter is applied before the filters in `ephemeral`, `state`, `timeline` or `accountData`
/// @param rooms a list of room IDs to include. If this list is absent then all rooms are included.
///   This filter is applied before the filters in `ephemeral`, `state`, `timeline` or `accountData`
/// @param state the state events to include for rooms.
/// @param timeline the message and state update events to include for rooms.
public record RoomFilter(
    RoomEventFilter accountData,
    RoomEventFilter ephemeral,
    Boolean includeLeave,
    List<String> notRooms,
    List<String> rooms,
    RoomEventFilter state,
    RoomEventFilter timeline) {}
