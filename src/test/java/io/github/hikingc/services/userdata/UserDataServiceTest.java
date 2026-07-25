package io.github.hikingc.services.userdata;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.hikingc.matrixsdk.api.MatrixClient;
import io.github.hikingc.matrixsdk.api.identifiers.UserID;
import io.github.hikingc.matrixsdk.api.userdata.UserProfile;
import io.github.hikingc.matrixsdk.context.DiscoveryResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class UserDataServiceTest {

    private static MatrixClient client;


    private static final String AUTH_TOKEN = "1234";
    private static final UserID USER_ID = UserID.parse("@user:example.com");
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

    @Test
    void searchUsersByTerm() {
        stubFor(post(urlEqualTo("/_matrix/client/v3/user_directory/search"))
                .willReturn(okJson("""
                        {
                          "results": [
                            {"user_id": "@searchterm:matrix.org", "display_name": "Search Term"}
                          ],
                          "limited": false
                        }
                        """)));

        var results = client.userData().searchUsersByTerm(10, "searchterm");

        assertThat(results).isNotNull();
        assertThat(results.results()).hasSize(1);
    }

    @Test
    void getUserProfile() {
        stubFor(get(urlEqualTo("/_matrix/client/v3/profile/" + USER_ID))
                .willReturn(okJson("""
                        {
                          "displayname": "Test User",
                          "avatar_url": "mxc://matrix.org/abc123"
                        }
                        """)));

        UserProfile profile = client.userData().getUserProfile(USER_ID);

        assertThat(profile).isNotNull();
        assertThat(profile.displayName()).isEqualTo("Test User");
    }

    @Test
    void getUserProfileByProperty() {
        stubFor(get(urlEqualTo("/_matrix/client/v3/profile/" + USER_ID + "/keyname"))
                .willReturn(okJson("{\"keyname\": \"valuename\"}")));

        String value = client.userData().getUserProfileByProperty(USER_ID, "keyname");

        assertThat(value).isEqualTo("valuename");
    }

    @Test
    void setUserProfileProperty() {
        stubFor(put(urlEqualTo("/_matrix/client/v3/profile/" + USER_ID + "/keyname"))
                .willReturn(aResponse().withStatus(200)));

        client.userData().setUserProfileProperty(USER_ID, "keyname", "valuename");

        verify(putRequestedFor(urlEqualTo("/_matrix/client/v3/profile/" + USER_ID + "/keyname"))
                .withRequestBody(equalToJson("{\"keyname\": \"valuename\"}")));
    }

    @Test
    void deleteUserProfileProperty() {
        stubFor(delete(urlEqualTo("/_matrix/client/v3/profile/" + USER_ID + "/keyname"))
                .willReturn(aResponse().withStatus(200)));

        client.userData().deleteUserProfileProperty(USER_ID, "keyname");

        verify(deleteRequestedFor(urlEqualTo("/_matrix/client/v3/profile/" + USER_ID + "/keyname")));
    }
}