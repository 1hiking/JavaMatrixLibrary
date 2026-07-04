package org.hik.services.modules;

import org.hik.api.MatrixAPIClientTest;
import org.hik.api.MatrixClient;
import org.hik.api.rooms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class RoomServiceTest extends MatrixAPIClientTest {

    private static final String USER = "test";
    private static final String AUTH_TOKEN = "1234";
    private static final String ROOM_ID = "!ekkTuJPNWnbuCJHvYB:kde.org";

    @BeforeEach
    void setUp() {
        wireMockServer.stubFor(get(urlEqualTo("/.well-known/matrix/client"))
                .willReturn(okJson("{\"m.homeserver\": {\"base_url\": \"" + wireMockServer.baseUrl() + "\"}}")));
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void sendCreateRequest_WithACorrectPayload_thenReturnARoomId() {
        String expectedRoomId = "!sefiuhWgwghwWgh:example.com";
        wireMockServer.stubFor(post("/_matrix/client/v3/createRoom")
                .withRequestBody(equalToJson("""
                        {
                          "creation_content": { "m.federate": true },
                          "name": "name",
                          "preset": "public_chat",
                          "room_alias_name": "alias",
                          "topic": "topic",
                          "visibility": "public"
                        }
                        """, true, true))
                .willReturn(okJson("""
                        { "room_id": "%s" }
                        """.formatted(expectedRoomId))));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().create(true, "name", "alias", "topic", CreationRoomType.PUBLIC_CHAT, true);

        assertEquals(expectedRoomId, response);
    }

    // -------------------------------------------------------------------------
    // alias management
    // -------------------------------------------------------------------------

    @Test
    void sendSetAliasRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        String alias = "#general:example.com";
        String encodedAlias = alias.replace("#", "%23");  // patch the uri

        wireMockServer.stubFor(put("/_matrix/client/v3/directory/room/" + encodedAlias)
                .withRequestBody(equalToJson("""
                        { "room_id": "%s" }
                        """.formatted(ROOM_ID), true, true))
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().setAlias(alias, ROOM_ID);

        wireMockServer.verify(putRequestedFor(urlEqualTo("/_matrix/client/v3/directory/room/" + encodedAlias)));
    }

    @Test
    void sendResolveAliasRequest_WithCorrectPayload_thenReturnResolvedAlias() {
        String alias = "#general:example.com";
        String encodedAlias = alias.replace("#", "%23");  // patch the uri

        wireMockServer.stubFor(get("/_matrix/client/v3/directory/room/" + encodedAlias)
                .willReturn(okJson("""
                        {
                          "room_id": "%s",
                          "servers": ["example.com", "other.org"]
                        }
                        """.formatted(ROOM_ID))));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().resolveAlias(alias);

        assertNotNull(response);
        assertEquals(ROOM_ID, response.roomId());
        assertFalse(response.servers().isEmpty());
    }

    @Test
    void sendDeleteAliasRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        String alias = "#general:example.com";
        String encodedAlias = alias.replace("#", "%23");  // patch the uri

        wireMockServer.stubFor(delete("/_matrix/client/v3/directory/room/" + encodedAlias)
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().deleteAlias(alias);

        wireMockServer.verify(deleteRequestedFor(urlEqualTo("/_matrix/client/v3/directory/room/" + encodedAlias)));
    }

    @Test
    void sendGetAliasesRequest_WithCorrectPayload_thenReturnAliases() {
        wireMockServer.stubFor(get("/_matrix/client/v3/rooms/" + ROOM_ID + "/aliases")
                .willReturn(okJson("""
                        {
                          "aliases": ["#general:example.com", "#main:example.com"]
                        }
                        """)));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().getAliasesOfARoom(ROOM_ID);

        assertNotNull(response);
        assertFalse(response.aliases().isEmpty());
        assertEquals(2, response.aliases().size());
    }

    // -------------------------------------------------------------------------
    // membership
    // -------------------------------------------------------------------------

    @Test
    void sendGetJoinedRoomsRequest_thenReturnJoinedRooms() {
        wireMockServer.stubFor(get("/_matrix/client/v3/joined_rooms")
                .willReturn(okJson("""
                        {
                          "joined_rooms": ["%s"]
                        }
                        """.formatted(ROOM_ID))));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().getJoinedRooms();

        assertNotNull(response);
        assertFalse(response.joinedRooms().isEmpty());
        assertEquals(ROOM_ID, response.joinedRooms().getFirst());
    }

    @Test
    void sendInviteRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/invite")
                .withRequestBody(equalToJson("""
                        {
                          "reason": "Welcome!",
                          "user_id": "@alice:example.com"
                        }
                        """, true, true))
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().inviteUser(ROOM_ID, new RoomMembershipRequest("Welcome!", "@alice:example.com"));

        wireMockServer.verify(postRequestedFor(
                urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/invite")));
    }

    @Test
    void sendJoinByRoomIdOrAliasRequest_WithCorrectPayload_thenReturnRoomId() {
        wireMockServer.stubFor(post("/_matrix/client/v3/join/" + ROOM_ID)
                .willReturn(okJson("""
                        { "room_id": "%s" }
                        """.formatted(ROOM_ID))));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().joinByRoomIdOrAliasIfAllowed(ROOM_ID, new JoinRoomRequest(null, null), null);

        assertNotNull(response);
        assertEquals(ROOM_ID, response);
    }

    @Test
    void sendJoinByRoomIdRequest_WithCorrectPayload_thenReturnRoomId() {
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/join")
                .willReturn(okJson("""
                        { "room_id": "%s" }
                        """.formatted(ROOM_ID))));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().joinByRoomIdIfAllowed(ROOM_ID, new JoinRoomRequest(null, null), null);

        assertNotNull(response);
        assertEquals(ROOM_ID, response);
    }

    @Test
    void sendKnockRequest_WithViaParams_thenReturnRoomId() {
        wireMockServer.stubFor(post(urlPathEqualTo("/_matrix/client/v3/knock/" + ROOM_ID))
                .withQueryParam("via", equalTo("server1.org"))
                .withRequestBody(matchingJsonPath("$.reason", equalTo("I want to join")))
                .willReturn(okJson("""
                        { "room_id": "%s" }
                        """.formatted(ROOM_ID))));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().knockOn(ROOM_ID, "I want to join", List.of("server1.org"));

        assertNotNull(response);
        assertEquals(ROOM_ID, response);
    }

    @Test
    void sendForgetRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/forget")
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().forget(ROOM_ID);

        wireMockServer.verify(postRequestedFor(
                urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/forget")));
    }

    @Test
    void sendLeaveRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/leave")
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().leave(ROOM_ID);

        wireMockServer.verify(postRequestedFor(
                urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/leave")));
    }

    @Test
    void sendKickRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/kick")
                .withRequestBody(equalToJson("""
                        {
                          "reason": "Test reason",
                          "user_id": "user"
                        }
                        """, true, true))
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().kick(ROOM_ID, new RoomMembershipRequest("Test reason", "user"));

        wireMockServer.verify(postRequestedFor(
                urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/kick")));
    }

    @Test
    void sendBanRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/ban")
                .withRequestBody(equalToJson("""
                        {
                          "reason": "Test reason",
                          "user_id": "user"
                        }
                        """, true, true))
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().ban(ROOM_ID, new RoomMembershipRequest("Test reason", "user"));

        wireMockServer.verify(postRequestedFor(
                urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/ban")));
    }

    @Test
    void sendUnbanRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        wireMockServer.stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/unban")
                .withRequestBody(equalToJson("""
                        {
                          "reason": "Test reason",
                          "user_id": "user"
                        }
                        """, true, true))
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().unban(ROOM_ID, new RoomMembershipRequest("Test reason", "user"));

        wireMockServer.verify(postRequestedFor(
                urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/unban")));
    }

    // -------------------------------------------------------------------------
    // directory
    // -------------------------------------------------------------------------

    @Test
    void sendGetRoomDirVisTypeRequest_WithCorrectPayload_thenReturnVisibility() {
        wireMockServer.stubFor(get("/_matrix/client/v3/directory/list/room/" + ROOM_ID)
                .willReturn(okJson("""
                        { "visibility": "public" }
                        """)));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().getRoomDirectoryVisibilityType(ROOM_ID);

        assertNotNull(response);
        assertEquals("public", response);
    }

    @Test
    void sendSetRoomDirVisTypeRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
        wireMockServer.stubFor(put("/_matrix/client/v3/directory/list/room/" + ROOM_ID)
                .willReturn(okJson("{}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        client.room().setRoomDirectoryVisibilityType(ROOM_ID, VisibilityRoomType.PRIVATE);

        wireMockServer.verify(putRequestedFor(
                urlEqualTo("/_matrix/client/v3/directory/list/room/" + ROOM_ID)));
    }

    @Test
    void sendGetPublicRoomDirRequest_WithQueryParams_thenReturnDirectory() {
        wireMockServer.stubFor(get(urlPathEqualTo("/_matrix/client/v3/publicRooms"))
                .withQueryParam("server", equalTo("example.com"))
                .withQueryParam("limit", equalTo("1"))
                .willReturn(okJson("""
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
    void sendGetPublicRoomDirPostRequest_WithBody_thenReturnDirectory() {
        wireMockServer.stubFor(post("/_matrix/client/v3/publicRooms")
                .willReturn(okJson("""
                        {
                          "chunk": [
                            {
                              "room_id": "!abc123:example.com",
                              "name": "General",
                              "num_joined_members": 10,
                              "world_readable": false,
                              "guest_can_join": false,
                              "join_rule": "public"
                            }
                          ],
                          "total_room_count_estimate": 1
                        }
                        """)));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var response = client.room().getPublishedRoomDirectory(new PublicRoomRequest(null, null, 10, null, null));

        assertNotNull(response);
        assertFalse(response.chunk().isEmpty());
        assertEquals("!abc123:example.com", response.chunk().getFirst().roomId());
    }

    @Test
    void sendGetRoomSummaryRequest_WithViaParam_thenReturnSummary() {
        String roomIdOrAlias = "!abc123:example.com";
        wireMockServer.stubFor(get(urlPathEqualTo("/_matrix/client/v1/room_summary/" + roomIdOrAlias))
                .withQueryParam("via", equalTo("example.com"))
                .willReturn(okJson("""
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
        // fix: pass List<String> not URI
        var response = client.room().getRoomSummary(roomIdOrAlias, List.of("example.com"));

        assertNotNull(response);
        assertEquals("!abc123:example.com", response.roomId());
        assertEquals("General", response.name());
        assertEquals(42, response.numJoinedMembers());
    }
}