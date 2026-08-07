package io.github.hikingc.matrixsdk.api.events;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.hikingc.matrixsdk.api.events.model.*;
import io.github.hikingc.matrixsdk.api.events.model.RoomNameEvent;

/// Interface that enforces fields required by both State and Message events when the event is
/// retrieved from the server via the Client-Server API, or sent to an Application Service via the
/// Application Services API.
///
/// @param <T> the `m.` type event.
/// @see <a href="https://spec.matrix.org/latest/client-server-api/#room-event-format">The room
///   event format as defined in the specification.</a>
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RoomCreateEvent.class, name = "m.room.create"),
        @JsonSubTypes.Type(value = RoomMemberEvent.class, name = "m.room.member"),
        @JsonSubTypes.Type(value = RoomPowerLevelsEvent.class, name = "m.room.power_levels"),
        @JsonSubTypes.Type(value = RoomJoinRulesEvent.class, name = "m.room.join_rules"),
        @JsonSubTypes.Type(value = RoomHistoryVisibilityEvent.class, name = "m.room.history_visibility"),
        @JsonSubTypes.Type(value = RoomGuestAcessEvent.class, name = "m.room.guest_access"),
        @JsonSubTypes.Type(value = RoomNameEvent.class, name = "m.room.name"),
        @JsonSubTypes.Type(value = RoomTopicEvent.class, name = "m.room.topic"),
        @JsonSubTypes.Type(value = RoomAvatarEvent.class, name = "m.room.avatar"),
        @JsonSubTypes.Type(value = RoomCanonicalAliasEvent.class, name = "m.room.canonical_alias"),
        @JsonSubTypes.Type(value = RoomPinnedEventsEvent.class, name = "m.room.pinned_events"),
        @JsonSubTypes.Type(value = RoomMessageEvent.class, name = "m.room.message"),
})
// This interface CANNOT permit anything other than these two.
public sealed interface ClientEvent<T> permits StateEvent, MessageEvent {

  /// @return the body of this event, as created by the user which sent it.
  T content();

  /// @return the globally unique identifier for this event.
  String eventId();

  /// @return timestamp (in milliseconds since the Unix epoch) on originating homeserver when this
  ///   event was sent.
  Long originServerTs();

  /// @return the ID of the room associated with this event.
  String roomId();

  /// @return contains the fully-qualified ID of the user who sent this event.
  String sender();

  /// @return the type of the event.
  String type();

  /// @return optional extra information about the event.
  UnsignedData unsigned();
}
