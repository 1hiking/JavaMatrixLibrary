package io.github.hikingc.matrixsdk.api.filters;

import java.util.List;

/// Signals what to filter by from an event. Recommended to use in addition with [RoomEventFilter]
/// for more granular filters.
///
/// @param limit The maximum number of events to return, **must be an integer greater than 0**.
///
///   Servers should apply a default value, and impose a maximum value to avoid resource exhaustion.
/// @param notSenders a list of sender IDs to exclude. If this list is absent then no senders are
///   excluded. A matching sender will be excluded even if it is listed in the senders filter.
/// @param notTypes a list of event types to exclude. If this list is absent then no event types are
///   excluded. A matching type will be excluded even if it is listed in the types filter. A * can
///   be used as a wildcard to match any sequence of characters.
/// @param senders a list of senders IDs to include. If this list is absent then all senders are
///   included.
/// @param types a list of event types to include. If this list is absent then all event types are
///   included. A "*" can be used as a wildcard to match any sequence of characters.
public record EventFilter(
    Integer limit,
    List<String> notSenders,
    List<String> notTypes,
    List<String> senders,
    List<String> types) {
  public EventFilter {
    if (limit <= 0) {
      throw new IllegalArgumentException("Field limit must be set above 0.");
    }
  }
}
