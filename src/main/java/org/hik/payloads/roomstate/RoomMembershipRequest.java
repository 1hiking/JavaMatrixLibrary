package org.hik.payloads.roomstate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/// This record represents the required values to be supplied to actions like banning or kicking.
///
/// @param reason The reason of the expulsion, the target will receive this message.
/// @param userId The id of the target to expel.
public record RoomMembershipRequest(String reason, @JsonProperty("user_id") String userId) {

    /// Compact constructor designed to validate nullity.
    ///
    /// @param reason The reason of the expulsion, the target will receive this message.
    /// @param userId The id of the target to expel.
    /// @throws NullPointerException if either value is null
    public RoomMembershipRequest {
        Objects.requireNonNull(reason, "roomId cannot be null");
        Objects.requireNonNull(userId, "roomAlias cannot be null");
    }
}
