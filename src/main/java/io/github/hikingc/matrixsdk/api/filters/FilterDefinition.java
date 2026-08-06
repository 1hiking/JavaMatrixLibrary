package io.github.hikingc.matrixsdk.api.filters;

import java.util.List;

/// Data points that can be used to restrict which events are returned to the client.
///
/// @param accountData the user account data that isn’t associated with rooms to include.
/// @param eventFields [List] of event fields to include. If this list is absent then all fields are
///   included. The entries are **dot-separated paths** for each property to include. So
///   `['content.body']` will include the body field of the content object. A server may include
///   more fields than were requested.
/// @param eventFormat The format to use for events. `client` will return the events in a format
///   suitable for clients. `federation` will return the raw event as received over federation. The
///   default is `client`.
/// @param presence the presence updates to include.
/// @param room filters to be applied to room data.
public record FilterDefinition(
    EventFilter accountData,
    List<String> eventFields,
    String eventFormat,
    EventFilter presence,
    RoomFilter room) {}
