package io.github.hikingc.matrixsdk.api.userdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import org.jspecify.annotations.NonNull;

/// Represents the user information requested from a directory search request.
///
/// Only data field guaranteed to be available is the user's id.
///
/// @param avatarUrl an avatar [URI] prefixed with
///   [mxc://](https://spec.matrix.org/v1.18/client-server-api/#matrix-content-mxc-uris).
/// @param displayName their display name.
/// @param userId their matrix User ID.
public record User(
    URI avatarUrl, String displayName, @NonNull @JsonProperty(required = true) String userId) {}
