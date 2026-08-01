package io.github.hikingc.matrixsdk.api.events;

import io.github.hikingc.matrixsdk.api.events.states.*;

/// Interface that represents all state events which hold an empty `state_key` [String].
///
/// @param <C> a Record that represents the `content` of the event.
public sealed interface SingletonStateEvent<C> extends DeserializedEvent<C> permits RoomAvatarEvent, RoomCanonicalAliasEvent, RoomCreateEvent, RoomGuestAcessEvent, RoomHistoryVisibilityEvent, RoomJoinRulesEvent, RoomMemberEvent, RoomNameEvent, RoomPinnedEventEvents, RoomPowerLevelsEvent, RoomTopicEvent {

    default String stateKey() {
        return "";
    }
}
