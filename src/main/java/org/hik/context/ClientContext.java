package org.hik.context;

import org.hik.api.ClientCredentials;

public class ClientContext {
    private final ClientCredentials credentials;
    private final DiscoveryResponse discoveryResponse;

    public ClientContext(ClientCredentials credentials, DiscoveryResponse discoveryResponse) {
        this.credentials = credentials;
        this.discoveryResponse = discoveryResponse;
    }

    public ClientCredentials credentials() {
        return credentials;
    }

    public DiscoveryResponse discoveryResponse() {
        return discoveryResponse;
    }
}
