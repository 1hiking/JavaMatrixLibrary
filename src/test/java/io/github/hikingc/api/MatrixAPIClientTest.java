package io.github.hikingc.api;


import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.hikingc.matrixsdk.api.MatrixClient;
import io.github.hikingc.matrixsdk.context.DiscoveryResponse;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(InstancioExtension.class)
@WireMockTest
class MatrixAPIClientTest {
    private static final String AUTH_TOKEN = "1234";


    @BeforeEach
    void setUp(WireMockRuntimeInfo wireMockRuntimeInfo) {
        stubFor(get(urlEqualTo("/.well-known/matrix/client"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"m.homeserver\": {\"base_url\": \"" + wireMockRuntimeInfo.getHttpBaseUrl() + "\"}}")));

    }

    @Given
    private DiscoveryResponse discoveryResponse;

    @Test
    void getWellKnown_WithAllRequiredProperties_thenReturnCorrectSerialization(WireMockRuntimeInfo wireMockRuntimeInfo) {
        var client = MatrixClient.create(discoveryResponse, AUTH_TOKEN);
        assertDoesNotThrow(() -> client, "The client should not throw given a good url.");
    }


}