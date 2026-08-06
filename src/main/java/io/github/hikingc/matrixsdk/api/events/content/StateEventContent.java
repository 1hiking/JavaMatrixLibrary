package io.github.hikingc.matrixsdk.api.events.content;

/// Marker interface for input state events.
public sealed interface StateEventContent
    permits RoomAvatar,
        RoomCanonicalAlias,
        RoomCreate,
        RoomGuestAccess,
        RoomHistoryVisibility,
        RoomJoinRules,
        RoomMember,
        RoomName,
        RoomPinnedEvents,
        RoomPowerLevels,
        RoomTopic {}
