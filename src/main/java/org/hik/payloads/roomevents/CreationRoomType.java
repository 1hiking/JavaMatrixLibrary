package org.hik.payloads.roomevents;

public enum CreationRoomType {

    /**
     * Recommended to use this to configure when you want to make 1 to 1 conversations.
     */
    PRIVATE_CHAT("private_chat"),
    /**
     * Same as PRIVATE_CHAT except all invitees are given the same power level as the room creator.
     */
    TRUSTED_PRIVATE_CHAT("trusted_private_chat"),
    /**
     * For public access, unlike the other types, choosing this will forbid guest access.
     */
    PUBLIC_CHAT("public_chat");


    private final String value;

    CreationRoomType(String value) {
        this.value = value;
    }

    /**
     * Returns the string room type value ('private_chat', 'public_chat' or 'trusted_private_chat') expected by the Matrix homeserver.
     *
     * @return the parameter string
     */
    public String getValue() {
        return this.value;
    }
}
