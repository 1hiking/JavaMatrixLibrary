package org.hik.api;


import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class MatrixAPIClientTest {
    private static final String USER = "test";
    private static final String AUTH_TOKEN = "1234";


    @RegisterExtension
    protected static final WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig()
                    .dynamicPort()
                    .usingFilesUnderClasspath("wiremock"))
            .build();

    @BeforeEach
    void setUp() {
        wireMockServer.stubFor(get(urlEqualTo("/.well-known/matrix/client"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"m.homeserver\": {\"base_url\": \"" + wireMockServer.baseUrl() + "\"}}")));

    }


    @Test
    void getWellKnown_WithAllRequiredProperties_thenReturnCorrectSerialization() {
        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        assertDoesNotThrow(() -> client, "The client should not throw given a good url.");
    }

    @Test
    void getWellKnown_WithBadUrl_thenReturnAnException() {
        assertThrows(IllegalArgumentException.class, () -> MatrixClient.create("INCORRECT.ORG", USER, AUTH_TOKEN),
                "The client should throw when given a bad url.");
    }


}