package io.github.hikingc.matrixsdk.api.events.states;

public record RoomHistoryVisibility(String history_visibility) { //One of: [invited, joined, shared, world_readable].
}
