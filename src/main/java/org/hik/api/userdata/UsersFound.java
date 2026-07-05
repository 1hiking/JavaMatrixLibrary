package org.hik.api.userdata;

import java.net.URI;
import java.util.List;

/// @param limited
/// @param results
public record UsersFound(Boolean limited,
                         List<User> results) {

    /// @param avatarUrl
    /// @param displayName
    /// @param userId
    public record User(URI avatarUrl,
                       String displayName,
                       String userId) {
    }
}
