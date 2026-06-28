package org.hik.payloads.roomevents;

/// The chronological order keys used by some queries to inform the server on how should the payload be ordered
public enum ChronologicalDirectionType {
    /// Look forward in time (from the oldest message towards the newest messages).
    CHRONOLOGICAL_ORDER("f"),
    /// Look backward in time (from the newest message towards the oldest historical messages).
    REVERSE_CHRONOLOGICAL_ORDER("b");


    private final String value;

    ChronologicalDirectionType(String value) {
        this.value = value;
    }

    /// Returns the query value ('f' or 'b') expected by Matrix servers.
    ///
    /// @return the query value.
    public String getValue() {
        return this.value;
    }
}
