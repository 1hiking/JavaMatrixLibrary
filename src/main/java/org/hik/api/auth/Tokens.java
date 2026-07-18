package org.hik.api.auth;

public record Tokens(String authToken, String refreshToken) {
}
