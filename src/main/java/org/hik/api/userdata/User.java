package org.hik.api.userdata;

import java.net.URI;

/// Record that stores the user information requested by a directory search request.
/// Only data field guaranteed to be available is the user's id.
///
/// @param avatarUrl   an avatar [URI] prefixed with [mxc://](https://spec.matrix.org/v1.18/client-server-api/#matrix-content-mxc-uris).
/// @param displayName their display name.
/// @param userId      their matrix User ID.
public record User(URI avatarUrl,
                   String displayName,
                   String userId) {
}
