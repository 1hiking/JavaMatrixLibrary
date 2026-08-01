package io.github.hikingc.matrixsdk.api.events.states;

import java.util.List;

public record RoomCanonicalAlias(
        String alias,
        List<String> altAliases
) {
    public RoomCanonicalAlias {
        altAliases = altAliases == null ? List.of() : List.copyOf(altAliases);
    }
}
