package org.hik.api.identifiers;

import java.util.Objects;

public final class RoomID implements Validator {
    private final String opaqueId;
    private final String domain;

    private RoomID(String opaqueId, String domain) {
        this.opaqueId = opaqueId;
        this.domain = domain;
    }

    public static RoomID parse(String rawRoomId) {
        Objects.requireNonNull(rawRoomId, "Room ID" + " must not be null");

        Validator.validateSigilId(rawRoomId, '!', "Room ID", true);

        int colonIdx = rawRoomId.indexOf(':');
        if (colonIdx == -1) {
            throw new IllegalArgumentException("Room ID missing domain: " + rawRoomId);
        }
        return new RoomID(rawRoomId.substring(1, colonIdx), rawRoomId.substring(colonIdx + 1));
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
        var that = (RoomID) obj;
        return Objects.equals(this.opaqueId, that.opaqueId) &&
                Objects.equals(this.domain, that.domain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opaqueId, domain);
    }

}

