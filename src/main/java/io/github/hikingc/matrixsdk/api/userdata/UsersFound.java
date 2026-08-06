package io.github.hikingc.matrixsdk.api.userdata;

import java.util.List;

/// Contains a list of [Users][User] from a directory search.
///
/// @param limited it indicates if list was truncated by a requested limit.
/// @param results a [List] containing [Users][User] ordered by rank and then by profile
///   information.
public record UsersFound(Boolean limited, List<User> results) {}
