package org.hik.api.auth;

import java.net.URI;
import java.util.List;

public record AuthMetadata(List<String> accountManagementActionsSupported,
                           URI accountManagementUri,
                           URI authorizationEndpoint,
                           List<String> codeChallengeMethodsSupported,
                           URI deviceAuthorizationEndpoint,
                           List<String> grantTypesSupported,
                           URI issuer,
                           List<String> promptValuesSupported,
                           URI registrationEndpoint,
                           List<String> responseModesSupported,
                           List<String> responseTypesSupported,
                           URI revocationEndpoint,
                           URI tokenEndpoint) {
}