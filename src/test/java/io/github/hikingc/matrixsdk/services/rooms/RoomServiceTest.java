package io.github.hikingc.matrixsdk.services.rooms;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.hikingc.matrixsdk.api.MatrixClient;
import io.github.hikingc.matrixsdk.api.events.content.RoomPowerLevels;
import io.github.hikingc.matrixsdk.api.identifiers.RoomAlias;
import io.github.hikingc.matrixsdk.api.identifiers.RoomID;
import io.github.hikingc.matrixsdk.api.identifiers.Validator;
import io.github.hikingc.matrixsdk.api.rooms.*;
import io.github.hikingc.matrixsdk.api.rooms.queries.CreationRoomType;
import io.github.hikingc.matrixsdk.api.rooms.queries.JoinRoomRequest;
import io.github.hikingc.matrixsdk.api.rooms.queries.VisibilityRoomType;
import io.github.hikingc.matrixsdk.context.DiscoveryResponse;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@WireMockTest
class RoomServiceTest {

  private static final RoomID ROOM_ID = RoomID.parse("!ekkTuJPNWnbuCJHvYB:kde.org");
  private static final String AUTH_TOKEN = "1234";
  private static MatrixClient client;
  private static DiscoveryResponse DISCOVERY_RESPONSE;

  @BeforeAll
  static void setUpDiscovery(WireMockRuntimeInfo wireMockRuntimeInfo) {
    DISCOVERY_RESPONSE =
        new DiscoveryResponse(
            new DiscoveryResponse.HomeserverInfo(wireMockRuntimeInfo.getHttpBaseUrl()), null, null);
  }

  @BeforeEach
  void createClient() {
    client = MatrixClient.create(DISCOVERY_RESPONSE, AUTH_TOKEN);
  }

  // -------------------------------------------------------------------------
  // create
  // -------------------------------------------------------------------------

  @Test
  void sendCreateRequest_WithACorrectPayload_thenReturnARoomId() {
    String expectedRoomId = "!sefiuhWgwghwWgh:example.com";
    stubFor(
        post("/_matrix/client/v3/createRoom")
            .withRequestBody(
                equalToJson(
                    """
                            {
                              "visibility": "private",
                              "room_alias_name": "thepub",
                              "name": "The Grand Duke Pub",
                              "topic": "All about happy hour",
                              "invite": ["@alice:example.com", "@bob:example.com"],
                              "invite_3pid": [
                                {
                                  "id_server": "identity.example.com",
                                  "id_access_token": "abc123_OpaqueString",
                                  "medium": "email",
                                  "address": "alice@example.com"
                                }
                              ],
                              "room_version": "11",
                              "creation_content": {
                                "m.federate": false
                              },
                              "initial_state": [
                                {
                                  "type": "m.room.join_rules",
                                  "state_key": "",
                                  "content": {
                                    "join_rule": "public"
                                  }
                                },
                                {
                                  "type": "m.room.history_visibility",
                                  "state_key": "",
                                  "content": {
                                    "history_visibility": "shared"
                                  }
                                }
                              ],
                              "preset": "private_chat",
                              "is_direct": false,
                              "power_level_content_override": {
                                "users_default": 0,
                                "events_default": 0,
                                "state_default": 50,
                                "ban": 50,
                                "kick": 50,
                                "redact": 50,
                                "invite": 0,
                                "events": {
                                  "m.room.name": 50,
                                  "m.room.power_levels": 100
                                },
                                "users": {
                                  "@alice:example.com": 100
                                }
                              }
                            }
                            """,
                    true,
                    true))
            .willReturn(
                okJson(
                    """
                        { "room_id": "%s" }
                        """
                        .formatted(expectedRoomId))));
    InitialRoomConfiguration config = new InitialRoomConfiguration(
            new InitialRoomConfiguration.CreationContent(false), // m.federate: false
            List.of(
                    new InitialRoomConfiguration.StateEvent(
                            Map.of("join_rule", "public"),
                            "",
                            "m.room.join_rules"),
                    new InitialRoomConfiguration.StateEvent(
                            Map.of("history_visibility", "shared"),
                            "",
                            "m.room.history_visibility")
            ),
            List.of("@alice:example.com", "@bob:example.com"),
            List.of(
                    new InitialRoomConfiguration.Invite3pid(
                            "alice@example.com",
                            "abc123_OpaqueString",
                            "identity.example.com",
                            "email")
            ),
            false, // is_direct
            "The Grand Duke Pub",
            new RoomPowerLevels(
                    50,   // ban
                    Map.of(
                            "m.room.name", 50,
                            "m.room.power_levels", 100
                    ),    // events
                    0,    // eventsDefault
                    0,    // invite
                    50,   // kick
                    null, // notifications — not present in source JSON
                    50,   // redact
                    50,   // stateDefault
                    Map.of("@alice:example.com", 100), // users
                    0     // users_default
            ),
            CreationRoomType.PRIVATE_CHAT,
            "thepub",
            "11",
            "All about happy hour",
            VisibilityRoomType.PRIVATE
    );
    var response =
        client.room().create(config);
    assertEquals(expectedRoomId, response);
  }

  // -------------------------------------------------------------------------
  // alias management
  // -------------------------------------------------------------------------

  @Test
  void sendSetAliasRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    RoomAlias alias = RoomAlias.parse("#general:example.com");

    stubFor(
        put("/_matrix/client/v3/directory/room/%23general:example.com")
            .withRequestBody(
                equalToJson(
                    """
                        { "room_id": "%s" }
                        """
                        .formatted(ROOM_ID),
                    true,
                    true))
            .willReturn(okJson("{}")));

    client.room().setAlias(alias, ROOM_ID);

    verify(putRequestedFor(urlEqualTo("/_matrix/client/v3/directory/room/%23general:example.com")));
  }

  @Test
  void sendResolveAliasRequest_WithCorrectPayload_thenReturnResolvedAlias() {
    RoomAlias alias = RoomAlias.parse("#general:example.com");
    String expectedPath = "%23general:example.com";

    stubFor(
        get("/_matrix/client/v3/directory/room/" + expectedPath)
            .willReturn(
                okJson(
                    """
                        {
                          "room_id": "%s",
                          "servers": ["example.com", "other.org"]
                        }
                        """
                        .formatted(ROOM_ID))));

    var response = client.room().resolveAlias(alias);

    assertNotNull(response);
    assertEquals(ROOM_ID.toString(), response.roomId());
    assertFalse(response.servers().isEmpty());
  }

  @Test
  void sendDeleteAliasRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    RoomAlias alias = RoomAlias.parse("#general:example.com");

    stubFor(
        delete("/_matrix/client/v3/directory/room/%23general:example.com")
            .willReturn(okJson("{}")));

    client.room().deleteAlias(alias);

    verify(
        deleteRequestedFor(urlEqualTo("/_matrix/client/v3/directory/room/%23general:example.com")));
  }

  @Test
  void sendGetAliasesRequest_WithCorrectPayload_thenReturnAliases() {
    stubFor(
        get("/_matrix/client/v3/rooms/" + ROOM_ID + "/aliases")
            .willReturn(
                okJson(
                    """
                        {
                          "aliases": ["#general:example.com", "#main:example.com"]
                        }
                        """)));

    var response = client.room().getAliasesOfARoom(ROOM_ID);

    assertNotNull(response);
    assertFalse(response.isEmpty());
    assertEquals(2, response.size());
  }

  // -------------------------------------------------------------------------
  // membership
  // -------------------------------------------------------------------------

  @Test
  void sendGetJoinedRoomsRequest_thenReturnJoinedRooms() {
    stubFor(
        get("/_matrix/client/v3/joined_rooms")
            .willReturn(
                okJson(
                    """
                        {
                          "joined_rooms": ["%s"]
                        }
                        """
                        .formatted(ROOM_ID))));

    var response = client.room().getJoinedRooms();

    assertNotNull(response);
    assertFalse(response.isEmpty());
    assertEquals(ROOM_ID.toString(), response.getFirst());
  }

  @Test
  void sendInviteRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    stubFor(
        post("/_matrix/client/v3/rooms/" + ROOM_ID + "/invite")
            .withRequestBody(
                equalToJson(
                    """
                        {
                          "reason": "Welcome!",
                          "user_id": "@alice:example.com"
                        }
                        """,
                    true,
                    true))
            .willReturn(okJson("{}")));

    client.room().inviteUser(ROOM_ID, new RoomMembershipRequest("Welcome!", "@alice:example.com"));

    verify(postRequestedFor(urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/invite")));
  }

  @Test
  void sendJoinByRoomIdOrAliasRequest_WithCorrectPayload_thenReturnRoomId() {
    stubFor(
        post("/_matrix/client/v3/join/" + ROOM_ID)
            .willReturn(
                okJson(
                    """
                        { "room_id": "%s" }
                        """
                        .formatted(ROOM_ID))));

    var response =
        client.room().joinByRoomIdOrAliasIfAllowed(ROOM_ID, new JoinRoomRequest(null, null), null);

    assertNotNull(response);
    assertEquals(ROOM_ID.toString(), response);
  }

  @Test
  void sendJoinByRoomIdRequest_WithCorrectPayload_thenReturnRoomId() {
    stubFor(
        post("/_matrix/client/v3/rooms/" + ROOM_ID + "/join")
            .willReturn(
                okJson(
                    """
                        { "room_id": "%s" }
                        """
                        .formatted(ROOM_ID))));

    var response =
        client.room().joinByRoomIdIfAllowed(ROOM_ID, new JoinRoomRequest(null, null), null);

    assertNotNull(response);
    assertEquals(ROOM_ID.toString(), response);
  }

  @Test
  void sendKnockRequest_WithViaParams_thenReturnRoomId() {
    stubFor(
        post(urlPathEqualTo("/_matrix/client/v3/knock/" + ROOM_ID))
            .withQueryParam("via", equalTo("server1.org"))
            .withRequestBody(matchingJsonPath("$.reason", equalTo("I want to join")))
            .willReturn(
                okJson(
                    """
                        { "room_id": "%s" }
                        """
                        .formatted(ROOM_ID))));

    var response = client.room().knockOn(ROOM_ID, "I want to join", List.of("server1.org"));

    assertNotNull(response);
    assertEquals(ROOM_ID.toString(), response);
  }

  @Test
  void sendForgetRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/forget").willReturn(okJson("{}")));

    client.room().forget(ROOM_ID);

    verify(postRequestedFor(urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/forget")));
  }

  @Test
  void sendLeaveRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    stubFor(post("/_matrix/client/v3/rooms/" + ROOM_ID + "/leave").willReturn(okJson("{}")));

    client.room().leave(ROOM_ID);

    verify(postRequestedFor(urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/leave")));
  }

  @Test
  void sendKickRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    stubFor(
        post("/_matrix/client/v3/rooms/" + ROOM_ID + "/kick")
            .withRequestBody(
                equalToJson(
                    """
                        {
                          "reason": "Test reason",
                          "user_id": "user"
                        }
                        """,
                    true,
                    true))
            .willReturn(okJson("{}")));

    client.room().kick(ROOM_ID, new RoomMembershipRequest("Test reason", "user"));

    verify(postRequestedFor(urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/kick")));
  }

  @Test
  void sendBanRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    stubFor(
        post("/_matrix/client/v3/rooms/" + ROOM_ID + "/ban")
            .withRequestBody(
                equalToJson(
                    """
                        {
                          "reason": "Test reason",
                          "user_id": "user"
                        }
                        """,
                    true,
                    true))
            .willReturn(okJson("{}")));

    client.room().ban(ROOM_ID, new RoomMembershipRequest("Test reason", "user"));

    verify(postRequestedFor(urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/ban")));
  }

  @Test
  void sendUnbanRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    stubFor(
        post("/_matrix/client/v3/rooms/" + ROOM_ID + "/unban")
            .withRequestBody(
                equalToJson(
                    """
                        {
                          "reason": "Test reason",
                          "user_id": "user"
                        }
                        """,
                    true,
                    true))
            .willReturn(okJson("{}")));

    client.room().unban(ROOM_ID, new RoomMembershipRequest("Test reason", "user"));

    verify(postRequestedFor(urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/unban")));
  }

  // -------------------------------------------------------------------------
  // directory
  // -------------------------------------------------------------------------

  @Test
  void sendGetRoomDirVisTypeRequest_WithCorrectPayload_thenReturnVisibility() {
    stubFor(
        get("/_matrix/client/v3/directory/list/room/" + ROOM_ID)
            .willReturn(
                okJson(
                    """
                        { "visibility": "public" }
                        """)));

    var response = client.room().getRoomDirectoryVisibilityType(ROOM_ID);

    assertNotNull(response);
    assertEquals("public", response);
  }

  @Test
  void sendSetRoomDirVisTypeRequest_WithCorrectPayload_thenHitCorrectEndpoint() {
    stubFor(put("/_matrix/client/v3/directory/list/room/" + ROOM_ID).willReturn(okJson("{}")));

    client.room().setRoomDirectoryVisibilityType(ROOM_ID, VisibilityRoomType.PRIVATE);

    verify(putRequestedFor(urlEqualTo("/_matrix/client/v3/directory/list/room/" + ROOM_ID)));
  }

  @Test
  void sendGetPublicRoomDirRequest_WithQueryParams_thenReturnDirectory() {
    stubFor(
        get(urlPathEqualTo("/_matrix/client/v3/publicRooms"))
            .withQueryParam("server", equalTo("example.com"))
            .withQueryParam("limit", equalTo("1"))
            .willReturn(
                okJson(
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

    var response = client.room().getPublishedRoomDirectory(1, "example.com", null);

    assertNotNull(response);
    assertNotNull(response.chunk());
    assertEquals("!abc123:example.com", response.chunk().getFirst().roomId());
    assertEquals("General", response.chunk().getFirst().name());
    assertEquals(1, response.totalRoomCountEstimate());
  }

  @Test
  void sendGetPublicRoomDirPostRequest_WithBody_thenReturnDirectory() {
    stubFor(
        post("/_matrix/client/v3/publicRooms")
            .willReturn(
                okJson(
                    """
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

    var response =
        client.room().getPublishedRoomDirectory(new PublicRoomRequest(null, null, 10, null, null));

    assertNotNull(response);
    assertFalse(response.chunk().isEmpty());
    assertEquals("!abc123:example.com", response.chunk().getFirst().roomId());
  }

  @Test
  void sendGetRoomSummaryRequest_WithViaParam_thenReturnSummary() {
    Validator roomIdOrAlias = RoomID.parse("!abc123:example.com");
    stubFor(
        get(urlPathEqualTo("/_matrix/client/v1/room_summary/" + roomIdOrAlias))
            .withQueryParam("via", equalTo("example.com"))
            .willReturn(
                okJson(
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

    // fix: pass List<String> not URI
    var response = client.room().getRoomSummary(roomIdOrAlias, List.of("example.com"));

    assertNotNull(response);
    assertEquals("!abc123:example.com", response.roomId());
    assertEquals("General", response.name());
    assertEquals(42, response.numJoinedMembers());
  }
}
