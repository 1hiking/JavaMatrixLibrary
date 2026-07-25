package org.hik.api.identifiers;

import java.util.Objects;

public final class UserID implements Validator {
    private final String opaqueId;
    private final String domain;

    private UserID(String opaqueId, String domain) {
        this.opaqueId = opaqueId;
        this.domain = domain;
    }

    public static UserID parse(String rawUserId) {
        Objects.requireNonNull(rawUserId, "User ID" + " must not be null");

        Validator.validateSigilId(rawUserId, '@', "User ID", false);

        int colonIdx = rawUserId.indexOf(':');
        if (colonIdx == -1) {
            throw new IllegalArgumentException("User ID missing domain: " + rawUserId);
        }
        return new UserID(rawUserId.substring(1, colonIdx), rawUserId.substring(colonIdx + 1));
    }

    @Override
    public String toString() {
        return "!" + opaqueId + ":" + domain;
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
        var that = (UserID) obj;
        return Objects.equals(this.opaqueId, that.opaqueId) &&
                Objects.equals(this.domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opaqueId, domain);
    }

}

