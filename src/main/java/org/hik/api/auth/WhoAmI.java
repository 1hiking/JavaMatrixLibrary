package org.hik.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Holds information of the `/whoami` endpoint from the server.
///
/// @param deviceId the Device ID associated with the access token.
///
/// If no device is associated with the access token (such as in the case of application services) then this field can be omitted. Otherwise this is required.
/// @param isGuest when `true`, the user is a Guest User. When not present or `false`, the user is presumed to be a non-guest user.
/// @param userId the user ID that owns the access token.
public record WhoAmI(String deviceId,
                     String isGuest,
                     @JsonProperty(required = true) String userId) {
}
