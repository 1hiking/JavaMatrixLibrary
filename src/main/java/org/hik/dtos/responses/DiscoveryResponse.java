package org.hik.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscoveryResponse(
        @JsonProperty("m.homeserver") HomeserverInfo homeserver,
        @JsonProperty("m.identity_server") IdentityServerInfo identityServer,
        @JsonProperty("org.matrix.msc2965.authentication") AuthInfo authentication,
        @JsonProperty("org.matrix.msc4143.rtc_foci") List<RtcFocus> rtcFoci
) {
    public record HomeserverInfo(
            @JsonProperty("base_url") String baseUrl
    ) {
    }

    public record IdentityServerInfo(
            @JsonProperty("base_url") String baseUrl
    ) {
    }

    public record AuthInfo(
            String issuer,
            String account
    ) {
    }

    public record RtcFocus(
            String type,
            @JsonProperty("livekit_service_url") String livekitServiceUrl
    ) {
    }
}