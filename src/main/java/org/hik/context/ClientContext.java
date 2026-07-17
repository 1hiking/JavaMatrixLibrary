package org.hik.context;

/// ClientContext stores global context data for internal services.
///
/// @param token       The user token.
/// @param discoveryResponse The discovery response data.
public record ClientContext(String token, DiscoveryResponse discoveryResponse) {
}
