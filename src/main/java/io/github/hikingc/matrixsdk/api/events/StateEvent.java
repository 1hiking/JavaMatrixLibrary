package io.github.hikingc.matrixsdk.api.events;

import io.github.hikingc.matrixsdk.api.events.model.RoomMemberEvent;

/// These are events which update the metadata state of the room (e.g. room topic, room membership
/// etc.). State is keyed by a tuple of event type and a state_key. State in the room with the same
/// key-tuple will be overwritten.
///
/// @param <C>
public sealed interface StateEvent<C> extends ClientEvent<C>
    permits SingletonStateEvent,
        RoomMemberEvent { // Stripped state events are missing from this tree...

  String stateKey();
}
