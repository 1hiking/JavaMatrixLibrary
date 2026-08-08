package io.github.hikingc.matrixsdk.api.rooms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hikingc.matrixsdk.api.events.content.RoomPowerLevels;
import io.github.hikingc.matrixsdk.api.rooms.queries.CreationRoomType;
import io.github.hikingc.matrixsdk.api.rooms.queries.VisibilityRoomType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/// The configuration data that a server will follow to configure the room.
///
/// @param creationContent extra keys, currently only `m.federate` is mapped.
/// @param initialState a [List] of state events to set in the new room. This allows the user to
///   override the default state events set in the new room.
/// @param invite a [List] of UserIDs to invite to the room. The server will be responsible for
///   handling these invitations,
/// @param invite3pid a [List] of objects representing third-party IDs to invite into the room.
/// @param isDirect Sets a flag on `m.room.member` events. See the
///   [spec](https://spec.matrix.org/v1.18/client-server-api/#direct-messaging) for more
///   information,
/// @param name of the room. Overwrites `initialState`.
/// @param powerLevelContentOverride the power level content to override in the default power level
///   event. This object is applied on top of the generated `m.room.power_levels` event content
///   prior to it being sent to the room. Defaults to overriding nothing.
/// @param preset Convenience parameter for setting various default state events based on a
///   [CreationRoomType] value. If unset, it will use `visibility`.
/// @param roomAliasName if included, a room alias will be created and mapped to the newly created
///   room. The alias will belong on the same homeserver which created the room.
/// @param roomVersion to set for the room. If not provided, the homeserver is to use its configured
///   default. If provided, the homeserver will return a 400 error with the errcode
///   M_UNSUPPORTED_ROOM_VERSION if it does not support the room version.
/// @param topic of the room topic. Overwrites `initialState`.
/// @param visibility of the room. Defaults to private if unset.
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record InitialRoomConfiguration(
    CreationContent creationContent,
    List<StateEvent> initialState,
    List<String> invite,
    @JsonProperty("invite_3pid") List<Invite3pid> invite3pid,
    Boolean isDirect,
    String name,
    RoomPowerLevels powerLevelContentOverride,
    CreationRoomType preset,
    String roomAliasName,
    String roomVersion,
    String topic,
    VisibilityRoomType visibility) {
  /// Extra keys, such as m.federate, to be added to the content of the m.room.create event.
  ///
  /// @param isFederated If the room will be federated.
  public record CreationContent(@JsonProperty("m.federate") Boolean isFederated) {}

  /// A list of state events to set in the new room. This allows the user to override the default
  /// state events set in the new room.
  ///
  /// Takes precedence over events set by preset, but gets overridden by name and topic keys.
  ///
  /// @param content The content of the event.
  /// @param stateKey The state\_key of the state event. Defaults to an empty string.
  /// @param type The type of event to send.
  public record StateEvent(Object content, String stateKey, String type) {}

  /// Represents third-party IDs to invite to the room.
  ///
  /// @param address
  /// @param idAccessToken
  /// @param idServer
  /// @param medium
  public record Invite3pid(String address, String idAccessToken, String idServer, String medium) {}

  /// Creates a federated, empty and private room with the name and topic desired.
  ///
  /// @param name of the room.
  /// @param topic of the room.
  /// @return a private room preset, ready to be used.
  @NonNull
  public static InitialRoomConfiguration createRoomWithSaneDefaults(String name, String topic) {
    Objects.requireNonNull(name, "Room name must not be null");
    Objects.requireNonNull(topic, "Topic must not be null");
    return new InitialRoomConfiguration(
        new CreationContent(true),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        false,
        name,
        null,
        CreationRoomType.PRIVATE_CHAT,
        null,
        null,
        topic,
        VisibilityRoomType.PRIVATE);
  }
}
