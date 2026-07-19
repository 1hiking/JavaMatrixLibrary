package org.hik.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/// Holds the data from the access token response as defined in the OAuth 2.0 spec.
///
/// @param accessToken  the access token issued by the server.
/// @param tokenType    information on the type of token to use.
/// @param expiresIn    lifetime in seconds of the access token.
/// @param refreshToken used to obtain new access tokens using the same auth grant.
/// @param scope        the scope of the `access token`.
/// @see <a href="https://datatracker.ietf.org/doc/html/rfc6749#section-5.1"> the RFC 6749 OAuth framework spec</a>
public record TokenMetadata(@JsonProperty(required = true) String accessToken,
                            @JsonProperty(required = true) String tokenType,
                            Integer expiresIn,
                            String refreshToken,
                            String scope) {
}
