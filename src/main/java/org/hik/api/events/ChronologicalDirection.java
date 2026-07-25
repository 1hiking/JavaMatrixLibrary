package org.hik.api.events;

/// Chronological order keys used to inform the server on how should the messages be ordered.
public enum ChronologicalDirection {
    /// Look forwards in time (from oldest to newest messages).
    CHRONOLOGICAL_ORDER("f"),
    /// Look backwards in time (from newest to oldest messages).
    REVERSE_CHRONOLOGICAL_ORDER("b");


    private final String value;

    ChronologicalDirection(String value) {
        this.value = value;
    }

    /// The query value ('f' or 'b').
    ///
    /// @return the query value.
    public String getValue() {
        return this.value;
    }
}
