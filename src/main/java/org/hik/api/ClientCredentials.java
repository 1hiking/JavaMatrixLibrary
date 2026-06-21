package org.hik.api;


/// @param baseUrl  The base url
/// @param username The name of the user
/// @param token    Their auth token
public record ClientCredentials(String baseUrl, String username, String token) {
}