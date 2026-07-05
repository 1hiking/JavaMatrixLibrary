package org.hik.api.userdata;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/// This record defines all key parameters of a user's profile. The server might respond with additional profile fields
/// which will be deserialized in the additionalFields map.
///
/// @param avatarUrl        the user’s avatar URL if they have set one, otherwise not present.
/// @param displayName      the user’s display name if they have set one, otherwise not present.
/// @param mTz              the user’s time zone.
/// @param additionalFields additional profile fields.
public record UserProfile(
        URI avatarUrl,
        String displayName,
        String mTz,
        Map<String, Object> additionalFields
) {
    /// Deserialization helper to accommodate additional unknown fields.
    ///
    /// @param raw input key-values from a response.
    /// @return deserialized [UserProfile] with corresponding values.
    @JsonCreator
    public static UserProfile of(Map<String, Object> raw) {
        Map<String, Object> copy = new HashMap<>(raw);

        String avatarUrlStr = (String) copy.remove("avatar_url");
        URI avatarUrl = avatarUrlStr != null ? URI.create(avatarUrlStr) : null;

        String displayName = (String) copy.remove("displayname");
        String mTz = (String) copy.remove("m.tz");

        return new UserProfile(avatarUrl, displayName, mTz, Map.copyOf(copy));
    }
}
