package io.github.hikingc.api.identifiers;

import io.github.hikingc.matrixsdk.api.identifiers.RoomAlias;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


class RoomAliasTest {


    @ParameterizedTest
    @ValueSource(strings = {
            "#general:example.org",
            "#General:example.org",
            "#room.name:example.org",
            "#room_name:example.org",
            "#room-name:example.org",
            "#room+name:example.org",
            "#123room:example.org",
            "#a:example.org",
            "#general:localhost",
            "#general:matrix.example.org",
            "#general:example.org:8448",
            "#general:[2001:db8::1]",
            "#general:127.0.0.1"
    })
    void withValidStrings_ReturnRoomAlias(String alias) {
        assertDoesNotThrow(() -> RoomAlias.parse(alias));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "general:example.org",     // missing leading sigil
            "!general:example.org",    // wrong sigil (id, not alias)
            "#:example.org",           // empty localpart
            "#general",                // missing colon/domain
            "#general:",               // empty domain
            "#gen eral:example.org",   // whitespace in localpart
            "#general:exa mple.org",   // whitespace in domain
            "#general:example..org",   // malformed domain
            "#general:-example.org",   // domain label starts with hyphen
            "#general:example.org:99999", // port out of range
            "#general:[2001:db8::1",   // unterminated IPv6 literal
            "",
            "#"
    })
    void withInvalidStrings_ThrowsException(String alias) {
        assertThrows(IllegalArgumentException.class, () -> RoomAlias.parse(alias));
    }

    @ParameterizedTest
    @NullSource
    void withNull_ThrowsException(String alias) {
        assertThrows(NullPointerException.class, () -> RoomAlias.parse(alias));
    }

    @ParameterizedTest
    @EmptySource
    void withEmpty_ThrowsException(String alias) {
        assertThrows(IllegalArgumentException.class, () -> RoomAlias.parse(alias));
    }

    @Test
    void withNull_ThrowsNPE() {
        assertThrows(NullPointerException.class, () -> RoomAlias.parse(null));
    }
}