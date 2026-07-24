package org.hik.services.filtering;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.hik.api.MatrixClient;
import org.hik.api.filters.FilterDefinition;
import org.hik.context.DiscoveryResponse;
import org.hik.services.utils.Mapper;
import org.instancio.junit.Given;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import tools.jackson.databind.ObjectMapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(InstancioExtension.class)
@WireMockTest
class FilterServiceTest {

    private static MatrixClient client;
    private static final ObjectMapper mapper = Mapper.getInstance();
    private static final String AUTH_TOKEN = "1234";
    private static final String USER_ID = "@matrix:example.org";
    private static DiscoveryResponse DISCOVERY_RESPONSE;


    @BeforeAll
    static void setUpDiscovery(WireMockRuntimeInfo wireMockRuntimeInfo) {
        DISCOVERY_RESPONSE = new DiscoveryResponse(
                new DiscoveryResponse.HomeserverInfo(wireMockRuntimeInfo.getHttpBaseUrl()),
                null, null
        );
    }

    @BeforeEach
    void createClient() {
        client = MatrixClient.create(DISCOVERY_RESPONSE, AUTH_TOKEN);
    }

    @Given
    private FilterDefinition filterDefinition;

    @Test
    void publishFilter_WithACorrectPayload_thenReturnAnId() {
        String json = mapper.writeValueAsString(filterDefinition);
        stubFor(post("/_matrix/client/v3/user/" + USER_ID + "/filter")
                .withRequestBody(equalToJson(json))
                .willReturn(okJson("""
                        {
                          "filter_id": "66696p746572"
                        }""")));

        String response = client.filter().publishFilter(USER_ID, filterDefinition);

        assertNotNull(response);
    }

    @Test
    void getFilter_WithACorrectPayload_ThenReturnAFilterDefinition() {
        final String FILTER_ID = "ABC123";
        String json = mapper.writeValueAsString(filterDefinition);
        stubFor(get("/_matrix/client/v3/user/" + USER_ID + "/filter/" + FILTER_ID)
                .willReturn(okJson(json)));

        FilterDefinition response = client.filter().getFilter(USER_ID, FILTER_ID);

        assertNotNull(response);
    }
}
