package org.hik.api.userdata;

import java.net.URI;
import java.util.List;


public record UsersFound(Boolean limited,
                         List<User> results) {


    public record User(URI avatarUrl,
                       String displayName,
                       String userId) {
    }
}
