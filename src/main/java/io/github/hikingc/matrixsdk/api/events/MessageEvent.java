package io.github.hikingc.matrixsdk.api.events;

import io.github.hikingc.matrixsdk.api.events.model.RoomMessageEvent;

/// Interface for events which describe transient “once-off” activity in a room: typically
/// communication such as sending an instant message or setting up a VoIP call.
public sealed interface MessageEvent<C> extends ClientEvent<C> permits RoomMessageEvent {}
