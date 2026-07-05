package org.hik.services.userdata;

import org.hik.api.MatrixAPIClientTest;
import org.hik.api.MatrixClient;
import org.hik.api.userdata.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class UserDataServiceTest extends MatrixAPIClientTest {

    private static final String USER = "test";
    private static final String AUTH_TOKEN = "1234";

    private MatrixClient client;

    @BeforeEach
    void setUp() {
        wireMockServer.stubFor(get(urlEqualTo("/.well-known/matrix/client"))
                .willReturn(okJson("{\"m.homeserver\": {\"base_url\": \"" + wireMockServer.baseUrl() + "\"}}")));

        client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
    }

    @Test
    void searchUsersByTerm() {
        wireMockServer.stubFor(post(urlEqualTo("/_matrix/client/v3/user_directory/search"))
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
        wireMockServer.stubFor(get(urlEqualTo("/_matrix/client/v3/profile/userid"))
                .willReturn(okJson("""
                        {
                          "displayname": "Test User",
                          "avatar_url": "mxc://matrix.org/abc123"
                        }
                        """)));

        UserProfile profile = client.userData().getUserProfile("userid");

        assertThat(profile).isNotNull();
        assertThat(profile.displayName()).isEqualTo("Test User");
    }

    @Test
    void getUserProfileByProperty() {
        wireMockServer.stubFor(get(urlEqualTo("/_matrix/client/v3/profile/userid/keyname"))
                .willReturn(okJson("{\"keyname\": \"valuename\"}")));

        String value = client.userData().getUserProfileByProperty("userid", "keyname");

        assertThat(value).isEqualTo("valuename");
    }

    @Test
    void setUserProfileProperty() {
        wireMockServer.stubFor(put(urlEqualTo("/_matrix/client/v3/profile/userid/keyname"))
                .willReturn(aResponse().withStatus(200)));

        client.userData().setUserProfileProperty("userid", "keyname", "valuename");

        wireMockServer.verify(putRequestedFor(urlEqualTo("/_matrix/client/v3/profile/userid/keyname"))
                .withRequestBody(equalToJson("{\"keyname\": \"valuename\"}")));
    }

    @Test
    void deleteUserProfileProperty() {
        wireMockServer.stubFor(delete(urlEqualTo("/_matrix/client/v3/profile/userid/keyname"))
                .willReturn(aResponse().withStatus(200)));

        client.userData().deleteUserProfileProperty("userid", "keyname");

        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/_matrix/client/v3/profile/userid/keyname")));
    }
}