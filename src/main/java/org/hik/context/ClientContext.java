package org.hik.context;

import org.hik.api.ClientCredentials;

/// Utility record used to store context data for internal modules.
///
/// @param credentials       The user credentials.
/// @param discoveryResponse The discovery response data.
public record ClientContext(ClientCredentials credentials, DiscoveryResponse discoveryResponse) {
}
