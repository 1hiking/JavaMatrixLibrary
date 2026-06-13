package org.hik.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 *
 * Record to store discovery information about the domain.
 * This includes non-spec keys such as {@code org.matrix.msc2965.authentication} and {@code org.matrix.msc4143.rtc_foci}
 *
 * @param homeserver     Used to discover homeserver information.
 * @param identityServer Used to discover identity server information.
 * @param rtcFoci Used to store Matrix RTC data that's currently not on spec
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscoveryResponse(
        @JsonProperty("m.homeserver") HomeserverInfo homeserver,
        @JsonProperty("m.identity_server") IdentityServerInfo identityServer,
        @JsonProperty("org.matrix.msc4143.rtc_foci") List<RtcFocus> rtcFoci
) {
    /**
     * @param baseUrl The base URL for the homeserver for client-server connections.
     */
    public record HomeserverInfo(
            @JsonProperty("base_url") String baseUrl
    ) {
    }

    /**
     * @param baseUrl The base URL for the identity server for client-server connections.
     */
    public record IdentityServerInfo(
            @JsonProperty("base_url") String baseUrl
    ) {
    }


    public record RtcFocus(
            String type,
            @JsonProperty("livekit_service_url") String livekitServiceUrl
    ) {
    }
}