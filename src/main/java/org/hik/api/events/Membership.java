package org.hik.api.events;

/// Membership keys used to filter membership to include or exclude in the request.
public enum Membership {
    JOIN("join"),
    INVITE("invite"),
    KNOCK("knock"),
    LEAVE("leave"),
    BAN("ban");

    private final String value;

    Membership(String value) {
        this.value = value;
    }

    /// The kind of membership value.
    ///
    /// @return the membership value.
    public String getValue() {
        return this.value;
    }
}
