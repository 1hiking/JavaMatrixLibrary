package org.hik.api.events.states;

import java.util.List;

public record MatrixCanonicalAliasContent(
        String alias,
        List<String> altAliases
) {
    public MatrixCanonicalAliasContent {
        altAliases = altAliases == null ? List.of() : List.copyOf(altAliases);
    }
}
