package org.hik.payloads.roomstate;

/// The new visibility type for the room.
public enum VisibilityRoomType {
    /// Set the visibility private.
    PRIVATE("private"),
    /// Set the visibility to public.
    PUBLIC("public");


    private final String value;

    VisibilityRoomType(String value) {
        this.value = value;
    }

    /// Returns the parameter value ('private' or 'public') expected by Matrix servers.
    ///
    /// @return the parameter visibility value.
    public String getValue() {
        return this.value;
    }
}
