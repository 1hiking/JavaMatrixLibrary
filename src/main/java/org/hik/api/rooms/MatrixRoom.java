package org.hik.api.rooms;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/// This record represents the configuration that the server will follow to configure the room. Not all possible
/// parameters
/// are implemented but the most essential ones are.
///
/// @param creationContent Extra keys, currently only m.federate is mapped.
/// @param initialState    A list of state events to set in the new room. This allows the user to override the
/// default state events set in the new room.
/// @param userIds         An array of user IDs to invite to the room. The server will be responsible for handling
/// these invitations,
/// @param isDirect        Sets a flag on m.room.member events. See the [spec](https://spec.matrix.org/v1.18/client-server-api/#direct-messaging)
/// for more information,
/// @param name            Sets the name of the room. Overwrites `initialState`.
/// @param preset          Convenience parameter for setting various default state events based on a preset.
///                        If unset, it will use `visibility`, use [CreationRoomType] to determine which one.
/// @param roomAliasName   If this is included, a room alias will be created and mapped to the newly created room.
/// The alias will belong on the same homeserver which created the room.
/// @param topic           Sets the name of the room topic. Overwrites `initialState`.
/// @param visibility      Defaults to private if unset.
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record MatrixRoom(CreationContent creationContent,
                         StateEvent initialState,
                         List<String> userIds,
                         Boolean isDirect,
                         String name,
                         String preset,
                         String roomAliasName,
                         String topic,
                         String visibility) {
    /// Extra keys, such as m.federate, to be added to the content of the m.room.create event.
    ///
    /// @param isFederated If the room will be federated.
    public record CreationContent(@JsonProperty("m.federate") Boolean isFederated) {
    }

    /// A list of state events to set in the new room.
    /// This allows the user to override the default state events set in the new room.
    ///
    /// Takes precedence over events set by preset, but gets overridden by name and topic keys.
    ///
    /// @param content  The content of the event.
    /// @param stateKey The state\_key of the state event. Defaults to an empty string.
    /// @param type     The type of event to send.
    public record StateEvent(Object content, String stateKey, String type) {
    }
}
