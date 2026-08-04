package io.github.hikingc.matrixsdk.api.filters;

import io.github.hikingc.matrixsdk.api.Event;
import io.github.hikingc.matrixsdk.api.events.QueryParametersSync;

import java.net.URI;
import java.util.List;

/// Holds additional granular filters for events in a room.
///
/// @param containsUrl               if `true`, includes only events with a [URI] key in their content. If `false`, excludes those events. If omitted, [URI] key is not considered for filtering.
/// @param includeRedundantMembers   if `true`, sends all membership events for all events, even if they have already been sent to the client. Does not apply unless `lazyLoadMembers` is `true`. Defaults to `false`.
/// @param lazyLoadMembers           if `true`, enables lazy-loading of membership events. Defaults to `false`.
/// @param limit                     the maximum number of events to return, must be an integer greater than 0.
///
/// Servers should apply a default value, and impose a maximum value to avoid resource exhaustion.
/// @param notRooms                  a [List] of room IDs to exclude. If this [List] is absent then no rooms are excluded. A matching room will be excluded even if it is listed in the `rooms` filter.
/// @param notSenders                a [List] of sender IDs to exclude. If this [List] is absent then no senders are excluded. A matching sender will be excluded even if it is listed in the senders filter.
/// @param notTypes                  a [List] of event types to exclude. If this [List] is absent then no event types are excluded. A matching type will be excluded even if it is listed in the `types` filter. A "*" can be used as a wildcard to match any sequence of characters.
/// @param rooms                     a [List] of room IDs to include. If this [List] is absent then all rooms are included.
/// @param senders                   a [List] of senders IDs to include. If this [List] is absent then all senders are included.
/// @param types                     a [List] of event types to include. If this [List] is absent then all event types are included. A "*" can be used as a wildcard to match any sequence of characters.
/// @param unreadThreadNotifications if true, enables per-thread notification counts. Only applies to the [Event#sync(QueryParametersSync)] endpoint. Defaults to `false`.
/// @see <a href="https://spec.matrix.org/v1.19/client-server-api/#lazy-loading-room-members">Documentation about Lazy-loading room members.</a>
public record RoomEventFilter(Boolean containsUrl,
                              Boolean includeRedundantMembers,
                              Boolean lazyLoadMembers,
                              Integer limit,
                              List<String> notRooms,
                              List<String> notSenders,
                              List<String> notTypes,
                              List<String> rooms,
                              List<String> senders,
                              List<String> types,
                              Boolean unreadThreadNotifications) {
    public RoomEventFilter {
        if (limit <= 0) {
            throw new IllegalArgumentException("Field limit must be set above 0.");
        }
    }
}
