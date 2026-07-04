package org.hik.services.utils;

import java.util.Objects;

public final class Validator {
    private Validator() {
    }

    public static String roomId(String roomId) {
        return Objects.requireNonNull(roomId, "room Id must not be null");
    }

    public static String roomAlias(String roomAlias) {
        return Objects.requireNonNull(roomAlias, "room Alias must not be null");
    }

    public static String roomIdOrAlias(String roomIdOrAlias) {
        return Objects.requireNonNull(roomIdOrAlias, "room ID or Alias must not be null");
    }

    public static String userId(String userId) {
        return Objects.requireNonNull(userId, "user Id must not be null");
    }

    public static String token(String token) {
        return Objects.requireNonNull(token, "token must not be null");
    }

    public static <T> T notNull(T value, String name) {
        return Objects.requireNonNull(value, name + " must not be null");
    }
}
