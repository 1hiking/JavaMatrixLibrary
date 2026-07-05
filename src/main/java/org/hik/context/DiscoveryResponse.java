package org.hik.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/// This record is used to store discovery information about the domain.
///
/// This includes non-spec keys such as `org.matrix.msc4143.rtc_foci`
///
/// @param homeserver     Used to discover homeserver information.
/// @param identityServer Used to discover identity server information.
/// @param rtcFoci        Used to store Matrix RTC data that's currently not on spec
@JsonIgnoreProperties(ignoreUnknown = true)
public record DiscoveryResponse(
        @JsonProperty("m.homeserver") HomeserverInfo homeserver,
        @JsonProperty("m.identity_server") IdentityServerInfo identityServer,
        @JsonProperty("org.matrix.msc4143.rtc_foci") List<RtcFocus> rtcFoci
) {
    /// Record used to store homeserver information.
    ///
    /// @param baseUrl The base URL for the homeserver for client-server connections.
    public record HomeserverInfo(
            String baseUrl
    ) {
    }

    /// Record used to store identity server information.
    ///
    /// @param baseUrl The base URL for the identity server for client-server connections.
    public record IdentityServerInfo(
            String baseUrl
    ) {
    }

    /// Experimental record used to store rtc information.
    ///
    /// @param type              The type.
    /// @param livekitServiceUrl The livekit URL.
    public record RtcFocus(
            String type,
            String livekitServiceUrl
    ) {
    }
}