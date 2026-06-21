package org.hik.payloads.roomstate;

/// The chronological order keys used by some queries to inform the server on how should the payload be ordered
public enum ChronologicalDirectionEvent {
    /// Look forward in time (from the oldest message towards the newest messages).
    CHRONOLOGICAL_ORDER("f"),
    /// Look backward in time (from the newest message towards the oldest historical messages).
    REVERSE_CHRONOLOGICAL_ORDER("b");


    private final String value;

    ChronologicalDirectionEvent(String value) {
        this.value = value;
    }

    /// Returns the string query parameter value ('f' or 'b') expected by the Matrix homeserver.
    ///
    /// @return the raw query parameter string
    public String getValue() {
        return this.value;
    }
}
