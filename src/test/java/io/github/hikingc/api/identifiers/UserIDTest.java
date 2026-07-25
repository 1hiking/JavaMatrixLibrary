package io.github.hikingc.api.identifiers;

import io.github.hikingc.matrixsdk.api.identifiers.UserID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


class UserIDTest {


    @ParameterizedTest
    @ValueSource(strings = {
            "@example:example.org",
            "@example:matrix.org",
            "@a:example.org",
            "@user.name:example.org",
            "@user_name:example.org",
            "@user-name:example.org",
            "@user=name:example.org",
            "@user/name:example.org",
            "@user+name:example.org",
            "@123:example.org",
            "@example:localhost",
            "@example:matrix.example.org",
            "@example:example.org:8448",
            "@example:[2001:db8::1]",
            "@example:[2001:db8::1]:8448",
            "@example:127.0.0.1",
            "@example:127.0.0.1:8448"
    })
    void withValidStrings_ReturnUserID(String userId) {
        assertDoesNotThrow(() -> UserID.parse(userId));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "example:example.org",       // missing leading sigil
            "@:example.org",             // empty localpart
            "@example",                  // missing colon/domain
            "@example:",                 // empty domain
            "@ example:example.org",     // whitespace in localpart
            "@example :example.org",     // whitespace in localpart
            "@Example:example.org",      // uppercase not allowed in localpart (post-r0.0.0 grammar)
            "@example:exa mple.org",     // whitespace in domain
            "@example:example..org",     // malformed domain
            "@example:-example.org",     // domain label can't start with hyphen
            "@example:example.org-",     // domain label can't end with hyphen
            "@example:.org",             // domain missing label
            "@example:[2001:db8::1",     // unterminated IPv6 literal
            "@example:example.org:99999",// port out of range
            "",                           // empty string
            "@"                           // sigil only
    })
    void withInvalidStrings_ThrowsException(String userId) {
        assertThrows(IllegalArgumentException.class, () -> UserID.parse(userId));
    }

    @ParameterizedTest
    @NullSource
    void withNull_ThrowsException(String userId) {
        assertThrows(NullPointerException.class, () -> UserID.parse(userId));
    }

    @ParameterizedTest
    @EmptySource
    void withEmpty_ThrowsException(String userId) {
        assertThrows(IllegalArgumentException.class, () -> UserID.parse(userId));
    }

    @Test
    void withNull_ThrowsNPE() {
        assertThrows(NullPointerException.class, () -> UserID.parse(null));
    }
}