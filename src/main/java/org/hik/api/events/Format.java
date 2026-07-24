package org.hik.api.events;

/// Format keys used to define what format should states return as.
public enum Format {
    /// Returns only content of the state event.
    CONTENT("content"),
    /// Returns the entire event in the usual format suitable for clients, including fields like event ID, sender and timestamp.
    EVENT("event");

    private final String value;

    Format(String value) {
        this.value = value;
    }

    /// The kind of format value.
    ///
    /// @return the format value.
    public String getValue() {
        return this.value;
    }
}
