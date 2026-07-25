package io.github.hikingc.matrixsdk.api;

import io.github.hikingc.matrixsdk.api.auth.AuthMetadata;
import io.github.hikingc.matrixsdk.api.auth.WhoAmI;

/// Core interface for executing protocol operations to authenticate and server discovery.
///
/// @apiNote This is a Work-In-Progress interface
///
/// @see <a href="https://spec.matrix.org/v1.19/client-server-api/#client-authentication">Matrix Client-Server API Specification for Authentication</a>
/// @see <a href="https://spec.matrix.org/v1.19/client-server-api/#server-discovery">Matrix Client-Server API Specification for Server Discovery</a>
public interface Auth {

    AuthMetadata getAuthMetadata();

    WhoAmI getCurrentAccountInformation();

}
