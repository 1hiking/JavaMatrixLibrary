package org.hik.services.modules;

import org.hik.api.MatrixAPIClientTest;
import org.hik.api.MatrixClient;
import org.hik.payloads.roomstate.CreationRoomType;
import org.hik.payloads.roomstate.RoomMembershipRequest;
import org.hik.payloads.roomstate.VisibilityRoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class RoomServiceTest extends MatrixAPIClientTest {

    private static final String USER = "test";
    private static final String AUTH_TOKEN = "1234";


    @BeforeEach
    void setUp() {
        wireMockServer.stubFor(get(urlEqualTo("/.well-known/matrix/client"))
                .willReturn(okJson("{\"m.homeserver\": {\"base_url\": \"" + wireMockServer.baseUrl() + "\"}}")));

    }

    @Test
    void sendCreateRequest_WithACorrectPayload_thenReturnAString() throws InterruptedException {
        String expectedEventId = "!sefiuhWgwghwWgh:example.com";
        wireMockServer.stubFor(post("/_matrix/client/v3/createRoom")
                .withRequestBody(equalToJson("""
                        {
                          "creation_content": {
                            "m.federate": true
                          },
                          "name": "name",
                          "preset": "public_chat",
                          "room_alias_name": "alias",
                          "topic": "topic",
                          "visibility": "public"
                        }
                        """, true, true))
                .willReturn(okJson("""
                        {
                          "room_id": "%s"
                        }
                        """.formatted(expectedEventId))));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().create(true, "name", "alias", "topic", CreationRoomType.PUBLIC_CHAT, true);

        assertEquals(expectedEventId, response);

    }


    @Test
    void sendForgetRequest_WithCorrectPayload_thenReturnABoolean() throws InterruptedException {
        String room = "!ekkTuJPNWnbuCJHvYB:kde.org";
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + room + "/forget").willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().forget(room);
        assertTrue(response);
    }

    @Test
    void sendLeaveRequest_WithCorrectPayload_thenReturnABoolean() throws InterruptedException {
        String room = "!ekkTuJPNWnbuCJHvYB:kde.org";
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + room + "/leave").willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().leave(room);
        assertTrue(response);
    }

    @Test
    void sendKickRequest_WithCorrectPayload_thenReturnABoolean() throws InterruptedException {
        String room = "!ekkTuJPNWnbuCJHvYB:kde.org";
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + room + "/kick")
                .withRequestBody(equalToJson(
                        """
                                {
                                  "reason": "Test reason",
                                  "user_id": "user"
                                }
                                """, true, true
                )).willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().kick(room, new RoomMembershipRequest("Test reason", "user"));
        assertTrue(response);
    }

    @Test
    void sendBanRequest_WithCorrectPayload_thenReturnABoolean() throws InterruptedException {
        String room = "!ekkTuJPNWnbuCJHvYB:kde.org";
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + room + "/ban")
                .withRequestBody(equalToJson(
                        """
                                {
                                  "reason": "Test reason",
                                  "user_id": "user"
                                }
                                """, true, true
                )).willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().ban(room, new RoomMembershipRequest("Test reason", "user"));
        assertTrue(response);
    }

    @Test
    void sendUnbanRequest_WithCorrectPayload_thenReturnABoolean() throws InterruptedException {
        String room = "!ekkTuJPNWnbuCJHvYB:kde.org";
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + room + "/unban")
                .withRequestBody(equalToJson(
                        """
                                {
                                  "reason": "Test reason",
                                  "user_id": "user"
                                }
                                """, true, true
                )).willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().unban(room, new RoomMembershipRequest("Test reason", "user"));
        assertTrue(response);
    }

    @Test
    void sendRoomDirVisTypeRequest_WithCorrectPayload_thenReturnAString() throws InterruptedException {
        String room = "!ekkTuJPNWnbuCJHvYB:kde.org";
        wireMockServer.stubFor(get("/_matrix/client/v3/directory/list/room/" + room)
                .willReturn(okJson(
                        """
                                {
                                "visibility": "public"
                                }
                                """)));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().getRoomDirectoryVisibilityType(room);
        assertNotNull(response);
    }

    @Test
    void sendSetRoomDirVisTypeRequest_WithCorrectPayload_thenReturnABoolean() throws InterruptedException {
        String room = "!ekkTuJPNWnbuCJHvYB:kde.org";
        wireMockServer.stubFor(put("/_matrix/client/v3/directory/list/room/" + room)
                .willReturn(okJson(
                        """
                                {
                                "visibility": "public"
                                }
                                """)));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().setRoomDirectoryVisibilityType(room, VisibilityRoomType.PRIVATE);
        assertTrue(response);
    }

    @Test
    void sendRoomSummaryRequest_WithCorrectPayload_thenReturnAPayload() throws InterruptedException {
        wireMockServer.stubFor(get(
                urlPathEqualTo("/_matrix/client/v3/publicRooms"))
                .withQueryParam("server", equalTo("example.com"))
                .withQueryParam("limit", equalTo("1"))
                .willReturn(okJson(
                        """
                                {
                                    "chunk": [
                                        {
                                            "room_id": "!abc123:example.com",
                                            "name": "General",
                                            "topic": "A test room",
                                            "avatar_url": "mxc://example.com/abc123",
                                            "num_joined_members": 42,
                                            "world_readable": true,
                                            "guest_can_join": false,
                                            "join_rule": "public"
                                        }
                                    ],
                                    "next_batch": "p190q",
                                    "prev_batch": "p1902",
                                    "total_room_count_estimate": 1
                                }
                                """)));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().getPublishedRoomDirectory(1, "example.com", null);

        assertNotNull(response);
        assertNotNull(response.chunk());
        assertEquals("!abc123:example.com", response.chunk().getFirst().roomId());
        assertEquals("General", response.chunk().getFirst().name());
        assertEquals(1, response.totalRoomCountEstimate());
    }

    @Test
    void sendGetPubRoomDirRequest_WithCorrectPayload_thenReturnAString() throws InterruptedException {
        String roomIdOrAlias = "!abc123:example.com";
        wireMockServer.stubFor(get(
                urlPathEqualTo("/_matrix/client/v1/room_summary/" + roomIdOrAlias))
                .withQueryParam("via", equalTo("example.com"))
                .willReturn(okJson(
                        """
                        {
                            "room_id": "!abc123:example.com",
                            "canonical_alias": "#general:example.com",
                            "name": "General",
                            "topic": "A test room",
                            "avatar_url": "mxc://example.com/abc123",
                            "num_joined_members": 42,
                            "world_readable": true,
                            "guest_can_join": false,
                            "join_rule": "public",
                            "room_type": null,
                            "room_version": "10",
                            "membership": null
                        }
                        """)));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().getRoomSummary(roomIdOrAlias, URI.create("example.com"));

        assertNotNull(response);
        assertEquals("!abc123:example.com", response.roomId());
        assertEquals("General", response.name());
        assertEquals(42, response.numJoinedMembers());
    }


}