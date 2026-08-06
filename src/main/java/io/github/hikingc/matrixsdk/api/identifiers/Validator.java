package io.github.hikingc.matrixsdk.api.identifiers;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/// Common interface for all identifiers in the Matrix specification, it provides static methods for
/// validation
///
/// @see <a href="https://spec.matrix.org/v1.19/appendices/#identifier-grammar">Matrix definitions
///   and Grammar of Identifiers</a>
public interface Validator {
  /// The maximum value of bytes as defined in the specification.
  int MAX_BYTES = 255;

  /// Shared validation for Matrix identifiers of the form `<sigil><opaqueId>:<server_name>` (room
  /// ids, user ids, room aliases).
  ///
  /// Validates the sigil, presence of a separating colon, overall byte length, Unicode
  /// well-formedness, and that a server name actually follows the colon. Optionally restricts the
  /// opaqueId to alphanumeric characters.
  ///
  /// @param value the raw [String] to be evaluated.
  /// @param sigil the prefix of the ID.
  /// @param name it's name
  /// @param restrictLocalpartToAlphanumeric whether it should be evaluated against only
  ///   alphanumeric characters
  static void validateSigilId(
      String value, char sigil, String name, boolean restrictLocalpartToAlphanumeric) {
    Objects.requireNonNull(value, name + " must not be null");

    if (value.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
      throw new IllegalArgumentException(name + " exceeds " + MAX_BYTES + " bytes");
    }

    if (value.isEmpty()) {
      throw new IllegalArgumentException(name + " must not be empty");
    }

    if (value.charAt(0) != sigil) {
      throw new IllegalArgumentException(name + " must start with '" + sigil + "'");
    }

    int firstColon = value.indexOf(':');
    if (firstColon < 0) {
      throw new IllegalArgumentException(
          name + " must contain ':' separating opaqueId from server name");
    }

    // Check if we have strings with nothing between the sigil and the :
    if (value.indexOf(sigil) + 1 == firstColon) {
      throw new IllegalArgumentException(name + " must not have an empty opaqueId");
    }

    Validator.validateCodePoints(value, name);

    String localPart = value.substring(1, firstColon);
    String serverName = value.substring(firstColon + 1);
    if (!validateDomain(serverName)) {
      throw new IllegalArgumentException(name + " must contain a valid server name after ':'");
    }
    if (restrictLocalpartToAlphanumeric && !localPart.matches("[a-zA-Z0-9]+")) {
      throw new IllegalArgumentException(
          name + " opaqueId should only contain alphanumeric characters");
    }
  }

  /// The matrix specification defines as compliant any codepoint that contains valid non-surrogate
  /// Unicode code points, including control characters, except `:` and `NUL (U+0000)`
  ///
  /// @param value the raw [String] that is being validated.
  /// @param name the type of the ID being evaluated.
  static void validateCodePoints(String value, String name) {
    value
        .codePoints()
        .forEach(
            cp -> {
              if (!Character.isValidCodePoint(cp)) {
                throw new IllegalArgumentException(
                    "%s contains an invalid Unicode code point: U+%04X".formatted(name, cp));
              }
              if (cp >= 0xD800 && cp <= 0xDFFF) {
                throw new IllegalArgumentException(
                    "%s contains a lone surrogate code point: U+%04X".formatted(name, cp));
              }
              if (Character.isWhitespace(cp)) {
                throw new IllegalArgumentException("%s contains whitespace.".formatted(name));
              }
            });
  }

  /// Dirty check to ensure the servername is valid
  ///
  /// @param serverName an IPv4, IPv6, or valid hostname (with or without a port).
  /// @return whether if it's a valid domain based on [URI] rules
  static boolean validateDomain(String serverName) {
    if (serverName == null || serverName.isBlank()) {
      return false;
    }

    try {
      URI uri = new URI("scheme://" + serverName);

      String host = uri.getHost();
      int port = uri.getPort();

      if (host == null) {
        return false;
      }

      // No extra paths, queries or fragments
      if (uri.getPath() != null && !uri.getPath().isEmpty()) return false;
      if (uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null)
        return false;

      // Ensure port it's in valid range 1-65535
      if (serverName.contains(":") && !host.startsWith("[")) {
        // Handle non-IPv6 port check
        return port == -1 || (port >= 1 && port <= 65535);
      }

      return true;
    } catch (URISyntaxException _) {
      return false;
    }
  }
}
