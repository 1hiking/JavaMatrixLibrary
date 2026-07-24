package org.hik.services.utils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class Validator {

    private static final int MAX_BYTES = 255;

    private Validator() {
    }

    /// @param roomId a Room ID
    public static void roomId(String roomId) {
        validateSigilId(roomId, '!', "Room ID", true);
    }

    public static void userId(String userId) {
        validateSigilId(userId, '@', "User ID", false);
    }

    public static void roomAlias(String roomAlias) {
        validateSigilId(roomAlias, '#', "Room Alias", false);
    }

    public static void roomIdOrAlias(String roomIdOrAlias) {
        Objects.requireNonNull(roomIdOrAlias, "Room ID or Alias must not be null");
        if (roomIdOrAlias.isEmpty()) {
            throw new IllegalArgumentException("Room ID or Alias must not be empty");
        }
        switch (roomIdOrAlias.charAt(0)) {
            case '!' -> roomId(roomIdOrAlias);
            case '#' -> roomAlias(roomIdOrAlias);
            default -> throw new IllegalArgumentException(
                    "Value must start with '!' (room id) or '#' (room alias): " + roomIdOrAlias);
        }

    }

    public static <T> void notNull(T value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
    }

    /// Shared validation for Matrix identifiers of the form `<sigil><localpart>:<server_name>`
    /// (room ids, user ids, room aliases).
    ///
    /// Validates the sigil, presence of a separating colon, overall byte length, Unicode well-formedness,
    /// and that a server name actually follows the colon. Optionally restricts the localpart to alphanumeric characters.
    private static String validateSigilId(String value, char sigil, String name,
                                          boolean restrictLocalpartToAlphanumeric) {
        Objects.requireNonNull(value, name + " must not be null");

        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        if (value.charAt(0) != sigil) {
            throw new IllegalArgumentException(name + " must start with '" + sigil + "'");
        }
        int firstColon = value.indexOf(':');
        if (firstColon < 0) {
            throw new IllegalArgumentException(name + " must contain ':' separating localpart from server name");
        }
        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new IllegalArgumentException(name + " exceeds " + MAX_BYTES + " bytes");
        }

        validateCodePoints(value, name);

        String localPart = value.substring(1, firstColon);
        String serverName = value.substring(firstColon + 1);

        if (serverName.isEmpty()) {
            throw new IllegalArgumentException(name + " must contain a non-empty server name after ':'");
        }
        if (restrictLocalpartToAlphanumeric && !localPart.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException(name + " localpart should only contain alphanumeric characters");
        }

        return value;
    }

    private static void validateCodePoints(String value, String name) {
        value.codePoints().forEach(cp -> {
            if (!Character.isValidCodePoint(cp)) {
                throw new IllegalArgumentException(
                        "%s contains an invalid Unicode code point: U+%04X".formatted(name, cp));
            }
            if (cp >= 0xD800 && cp <= 0xDFFF) {
                throw new IllegalArgumentException(
                        "%s contains a lone surrogate code point: U+%04X".formatted(name, cp));
            }
        });
    }
}