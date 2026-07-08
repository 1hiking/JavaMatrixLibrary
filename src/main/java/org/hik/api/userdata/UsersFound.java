package org.hik.api.userdata;

import java.util.List;


/// Record used to contain the list of [Users][org.hik.api.userdata.User] from a directory search.
///
/// @param limited it indicates if list was truncated by a requested limit.
/// @param results a [List] containing [Users][org.hik.api.userdata.User] ordered by rank and then by profile
/// information.
public record UsersFound(Boolean limited,
                         List<User> results) {


}
