package io.github.hikingc.matrixsdk.api.identifiers;

import java.util.Objects;

/// This class allows for the representation and validation of a Room Alias in Matrix.
///
/// Their form is as  follows: `#room_alias:domain`.
///
/// The `localpart` of a [RoomAlias] **MAY** contain valid non-surrogate Unicode code points, except `:` and `NUL` (`U+0000`).
/// The localpart **SHOULD** only consist of alphanumeric characters (`A-Z, a-z, 0-9`) when generating them..
///
/// The `domain` of a [RoomAlias] is the server name of the homeserver which created the alias.
///
/// The length of a [RoomAlias], including the `#` sigil and the domain, **MUST NOT** exceed 255 bytes.
///
/// @see <a href="https://spec.matrix.org/v1.19/appendices/#room-aliases">Room Aliases as defined in the specification</a>
public final class RoomAlias implements Validator {
    private final String opaqueId;
    private final String domain;

    private RoomAlias(String opaqueId, String domain) {
        this.opaqueId = opaqueId;
        this.domain = domain;
    }

    /// Builds and validates a [RoomAlias]
    ///
    /// @param rawAliasId the [String] to validate.
    /// @return a [RoomAlias].
    /// @throws IllegalArgumentException if the [String] has broken a rule from the spec.
    /// @throws NullPointerException     if the [String] is null.
    public static RoomAlias parse(String rawAliasId) {
        Objects.requireNonNull(rawAliasId, "Alias ID" + " must not be null");

        Validator.validateSigilId(rawAliasId, '#', "Room Alias", false);

        int colonIdx = rawAliasId.indexOf(':');
        if (colonIdx == -1) {
            throw new IllegalArgumentException("Alias ID missing domain: " + rawAliasId);
        }
        return new RoomAlias(rawAliasId.substring(1, colonIdx), rawAliasId.substring(colonIdx + 1));
    }

    @Override
    public String toString() {
        return "#" + opaqueId + ":" + domain;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RoomAlias) obj;
        return Objects.equals(this.opaqueId, that.opaqueId) &&
                Objects.equals(this.domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opaqueId, domain);
    }

}
