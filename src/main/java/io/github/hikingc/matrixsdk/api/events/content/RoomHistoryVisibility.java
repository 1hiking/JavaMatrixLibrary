package io.github.hikingc.matrixsdk.api.events.content;

public record RoomHistoryVisibility(String history_visibility)
    implements StateEventContent { // One of: [invited, joined, shared, world_readable].
}
