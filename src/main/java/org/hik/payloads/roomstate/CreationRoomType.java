package org.hik.payloads.roomstate;

/// The room type keys used to tell the client what available preset should the server create the room with.
public enum CreationRoomType {

    /// Recommended to use this to configure when you want to make 1 to 1 conversations.
    PRIVATE_CHAT("private_chat"),
    /// Same as PRIVATE\_CHAT except all invitees are given the same power level as the room creator.
    TRUSTED_PRIVATE_CHAT("trusted_private_chat"),
    /// For public access, unlike the other types, choosing this will forbid guest access.
    PUBLIC_CHAT("public_chat");


    private final String value;

    CreationRoomType(String value) {
        this.value = value;
    }

    /// Returns the string room type value ('private\_chat', 'public\_chat' or 'trusted\_private\_chat') expected by the Matrix homeserver.
    ///
    /// @return the parameter string
    public String getValue() {
        return this.value;
    }
}
