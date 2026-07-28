package io.github.hikingc.matrixsdk.api.identifiers;

import java.util.Objects;

/// This class allows for the representation and validation of a User Identifier in Matrix.
///
/// Their ID is made up of two parts, the `localpart` and the `domain`
///
/// The `localpart` of a [UserID] is an opaque identifier for that user.
/// It **MUST NOT** be empty, and **MUST** contain only the characters `a-z`, `0-9`, `.`, `_`, `=`, `-`, `/`, and `+`.
///
/// The `domain` of a [UserID] is the server name of the homeserver which allocated the account.
///
/// The length of a [UserID], including the `@` sigil and the domain, **MUST NOT** exceed 255 bytes.
///
/// @see <a href="https://spec.matrix.org/v1.19/appendices/#user-identifiers">User Identifiers as defined in the specification</a>
public final class UserID implements Validator {
    private final String localpart;
    private final String domain;

    private UserID(String opaqueId, String domain) {
        this.localpart = opaqueId;
        this.domain = domain;
    }

    /// Builds and validates a [UserID]
    ///
    /// @param rawUserId the [String] to validate.
    /// @return a [UserID].
    /// @throws IllegalArgumentException if the [String] has broken a rule from the spec.
    /// @throws NullPointerException     if the [String] is null.
    public static UserID parse(String rawUserId) {
        Objects.requireNonNull(rawUserId, "User ID" + " must not be null");

        Validator.validateSigilId(rawUserId, '@', "User ID", false);

        if (rawUserId.chars().anyMatch(Character::isUpperCase)) {
            throw new IllegalArgumentException("User ID cannot have uppercase symbols: " + rawUserId);
        }

        int colonIdx = rawUserId.indexOf(':');
        if (colonIdx == -1) {
            throw new IllegalArgumentException("User ID missing domain: " + rawUserId);
        }
        return new UserID(rawUserId.substring(1, colonIdx), rawUserId.substring(colonIdx + 1));
    }

    @Override
    public String toString() {
        return "!" + localpart + ":" + domain;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UserID) obj;
        return Objects.equals(this.localpart, that.localpart) &&
                Objects.equals(this.domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localpart, domain);
    }

}

