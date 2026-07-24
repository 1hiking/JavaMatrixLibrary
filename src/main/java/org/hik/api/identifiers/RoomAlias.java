package org.hik.api.identifiers;

import java.util.Objects;

public final class RoomAlias implements Validator {
    private final String opaqueId;
    private final String domain;

    private RoomAlias(String opaqueId, String domain) {
        this.opaqueId = opaqueId;
        this.domain = domain;
    }

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

    public String opaqueId() {
        return opaqueId;
    }

    public String domain() {
        return domain;
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
