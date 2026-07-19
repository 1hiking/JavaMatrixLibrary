package org.hik.api.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;


/// Holds OAuth 2.0 server metadata fields that are relevant to the Matrix protocol and are defined in the specification,
///
/// @param accountManagementActionsSupported list of actions that the account management URL supports.
/// @param accountManagementUri              the [URI] where the user is able to access the account management capabilities of the homeserver.
/// @param authorizationEndpoint             the [URI] of the authorization endpoint, necessary to use the authorization code grant.
/// @param codeChallengeMethodsSupported     a list of OAuth 2.0 Proof Key for Code Exchange (PKCE) code challenge methods that the server supports at the authorization endpoint.
///
/// This array **MUST** contain at least the S256 value, for improved security in the authorization code grant.
/// @param deviceAuthorizationEndpoint       the [URI] of the device authorization endpoint, as defined in RFC 8628, necessary to use the <a href="https://spec.matrix.org/v1.18/client-server-api/#device-authorization-grant">device authorization grant.</a>
/// @param grantTypesSupported               a list of OAuth 2.0 grant type strings that the server supports at the token endpoint.
///
/// This array **MUST** contain at least the `authorization_code` and `refresh_token` values, for clients to be able to use the authorization code grant and refresh token grant, respectively.
/// @param issuer                            the authorization server’s issuer identifier, which is a URL that uses the `https` scheme and has no query or fragment components.
///
/// This is not used in the context of the Matrix specification, but is required by RFC 8414.
/// @param promptValuesSupported             list of OpenID Connect prompt values that the server supports at the authorization endpoint.
///
/// Only the `create` value defined in <a href="https://openid.net/specs/openid-connect-prompt-create-1_0.html">Initiating User Registration via OpenID Connect</a> is supported, for a client to signal to the server that the user desires to register a new account.
/// @param registrationEndpoint              [URI] of the client registration endpoint, necessary to perform dynamic registration of a client.
/// @param responseModesSupported            a list of OAuth 2.0 response mode strings that the server supports at the authorization endpoint.
///
/// This array **MUST** contain at least the `query` and `fragment` values, for improved security in the authorization code grant.
/// @param responseTypesSupported            a list of OAuth 2.0 response type strings that the server supports at the authorization endpoint.
///
/// This array **MUST** contain at least the `code` value, for clients to be able to use the authorization code grant.
/// @param revocationEndpoint                [URI] of the revocation endpoint, necessary to log out a client by invalidating its access and refresh tokens.
/// @param tokenEndpoint                     [URI] of the token endpoint, used by the grants.
public record AuthMetadata(List<String> accountManagementActionsSupported,
                           URI accountManagementUri,
                           @JsonProperty(required = true) URI authorizationEndpoint,
                           @JsonProperty(required = true) List<String> codeChallengeMethodsSupported,
                           URI deviceAuthorizationEndpoint,
                           @JsonProperty(required = true) List<String> grantTypesSupported,
                           URI issuer,
                           List<String> promptValuesSupported,
                           URI registrationEndpoint,
                           @JsonProperty(required = true) List<String> responseModesSupported,
                           @JsonProperty(required = true) List<String> responseTypesSupported,
                           @JsonProperty(required = true) URI revocationEndpoint,
                           @JsonProperty(required = true) URI tokenEndpoint) {
}