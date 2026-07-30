package io.github.hikingc.matrixsdk.services.events;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.hikingc.matrixsdk.api.MatrixClient;
import io.github.hikingc.matrixsdk.api.events.*;
import io.github.hikingc.matrixsdk.api.events.messages.File;
import io.github.hikingc.matrixsdk.api.events.messages.Text;
import io.github.hikingc.matrixsdk.api.identifiers.RoomID;
import io.github.hikingc.matrixsdk.context.DiscoveryResponse;
import io.github.hikingc.matrixsdk.exceptions.MatrixIOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class EventServiceTest {


    public static final RoomID ROOM_ID = RoomID.parse("!room:example.org");
    private static final String AUTH_TOKEN = "1234";
    private static DiscoveryResponse DISCOVERY_RESPONSE;
    private static MatrixClient client;

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

    private static Result getResult(Path tempDir) throws IOException {
        String roomMessageType = "m.room.message";
        String expectedEventId = "$h29asdf8q348hju9a:matrix.org";

        String serverName = "matrix.org";
        String mediaId = "fakeMediaId123";
        URI mockMxcUri = URI.create("mxc://" + serverName + "/" + mediaId);

        Path tempFile = tempDir.resolve("file.txt");
        Files.writeString(tempFile, "Test");
        return new Result(ROOM_ID, roomMessageType, expectedEventId, serverName, mediaId, mockMxcUri, tempFile);
    }

    @Test
    void getEvent_WithACorrectPayload_ThenReturnAClientEvent() {
        String eventId = "$143273582443PhrSn:example.org";
        stubFor(get("/_matrix/client/v3/rooms/" + ROOM_ID + "/event/" + eventId)
                .willReturn(okJson("""
                        {
                          "content": {
                            "body": "This is an example text message",
                            "format": "org.matrix.custom.html",
                            "formatted_body": "<b>This is an example text message</b>",
                            "msgtype": "m.text"
                          },
                          "event_id": "$143273582443PhrSn:example.org",
                          "origin_server_ts": 1432735824653,
                          "room_id": "!636q39766251:matrix.org",
                          "sender": "@example:example.org",
                          "type": "m.room.message",
                          "unsigned": {
                            "age": 1234,
                            "membership": "join"
                          }
                        }
                        """)));

        ClientEvent response = client.events().getEvent(ROOM_ID, eventId);
        assertThat(response).isNotNull();
        assertThat(response.eventId()).isEqualTo(eventId);
    }

    @Test
    void getJoinedMembers_WithACorrectPayload_ThenReturnARoomMembers() {
        stubFor(get("/_matrix/client/v3/rooms/" + ROOM_ID + "/joined_members")
                .willReturn(okJson("""
                        {
                          "joined": {
                            "@bar:example.com": {
                              "avatar_url": "mxc://riot.ovh/printErCATzZijQsSDWorRaK",
                              "display_name": "Bar"
                            }
                          }
                        }
                        
                        """)));
        var response = client.events().getJoinedMembers(ROOM_ID);
        assertThat(response).isNotNull();
    }

    @Test()
    void getMembers_WithACorrectPayload_thenReturnAListOfClientEvent() {
        final String TOKEN = "FAKE_PAGINATION_TOKEN";

        stubFor(get(urlPathEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/members"))
                .withQueryParams(
                        Map.ofEntries(
                                Map.entry("at", equalTo(TOKEN)),
                                Map.entry("membership", equalTo(Membership.JOIN.getValue())),
                                Map.entry("not_membership", equalTo(Membership.JOIN.getValue()))))
                .willReturn(okJson("""
                        {
                          "chunk": [
                            {
                              "content": {
                                "avatar_url": "mxc://example.org/SEsfnsuifSDFSSEF",
                                "displayname": "Alice Margatroid",
                                "membership": "join",
                                "reason": "Looking for support"
                              },
                              "event_id": "$143273582443PhrSn:example.org",
                              "origin_server_ts": 1432735824653,
                              "room_id": "!636q39766251:example.com",
                              "sender": "@alice:example.org",
                              "state_key": "@alice:example.org",
                              "type": "m.room.member",
                              "unsigned": {
                                "age": 1234,
                                "membership": "join"
                              }
                            }
                          ]
                        }
                        
                        """)));
        List<ClientEvent> response = client.events().getMembers(ROOM_ID, TOKEN, Membership.JOIN, Membership.JOIN);
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().eventId()).isEqualTo("$143273582443PhrSn:example.org");
    }

    @Test
    void getStateEvents_WithACorrectPaYload_thenReturnAListOfClientEvent() {
        stubFor(get("/_matrix/client/v3/rooms/" + ROOM_ID + "/state")
                .willReturn(okJson("""
                        [
                          {
                            "content": {
                              "join_rule": "public"
                            },
                            "event_id": "$143273582443PhrSn:example.org",
                            "origin_server_ts": 1432735824653,
                            "room_id": "!636q39766251:example.com",
                            "sender": "@example:example.org",
                            "state_key": "",
                            "type": "m.room.join_rules",
                            "unsigned": {
                              "age": 1234,
                              "membership": "join"
                            }
                          },
                          {
                            "content": {
                              "avatar_url": "mxc://example.org/SEsfnsuifSDFSSEF",
                              "displayname": "Alice Margatroid",
                              "membership": "join",
                              "reason": "Looking for support"
                            },
                            "event_id": "$143273582443PhrSn:example.org",
                            "origin_server_ts": 1432735824653,
                            "room_id": "!636q39766251:example.com",
                            "sender": "@alice:example.org",
                            "state_key": "@alice:example.org",
                            "type": "m.room.member",
                            "unsigned": {
                              "age": 1234,
                              "membership": "join"
                            }
                          },
                          {
                            "content": {
                              "m.federate": true,
                              "predecessor": {
                                "event_id": "$something:example.org",
                                "room_id": "!oldroom:example.org"
                              },
                              "room_version": "11"
                            },
                            "event_id": "$143273582443PhrSn:example.org",
                            "origin_server_ts": 1432735824653,
                            "room_id": "!636q39766251:example.com",
                            "sender": "@example:example.org",
                            "state_key": "",
                            "type": "m.room.create",
                            "unsigned": {
                              "age": 1234,
                              "membership": "join"
                            }
                          },
                          {
                            "content": {
                              "ban": 50,
                              "events": {
                                "m.room.name": 100,
                                "m.room.power_levels": 100
                              },
                              "events_default": 0,
                              "invite": 50,
                              "kick": 50,
                              "notifications": {
                                "room": 20
                              },
                              "redact": 50,
                              "state_default": 50,
                              "users": {
                                "@example:localhost": 100
                              },
                              "users_default": 0
                            },
                            "event_id": "$143273582443PhrSn:example.org",
                            "origin_server_ts": 1432735824653,
                            "room_id": "!636q39766251:example.com",
                            "sender": "@example:example.org",
                            "state_key": "",
                            "type": "m.room.power_levels",
                            "unsigned": {
                              "age": 1234,
                              "membership": "join"
                            }
                          }
                        ]
                        
                        """)));
        var response = client.events().getStateEvents(ROOM_ID);
        assertThat(response).isNotNull();
        assertThat(response).hasSize(4);

    }

    @Test
    void getStateEventsOverride_WithACorrectPaYload_thenReturnAListOfClientEvent() {
        final String EVENT_TYPE = "EVENT_TYPE";
        final String STATE_KEY = "STATE_KEY";
        stubFor(get("/_matrix/client/v3/rooms/" + ROOM_ID + "/state/" + EVENT_TYPE + "/" + STATE_KEY)
                .willReturn(okJson("""
                        {
                          "type": "m.room.name",
                          "event_id": "$143273582443PhrSn:example.org",
                          "sender": "@alice:example.org",
                          "origin_server_ts": 1432735824653,
                          "room_id": "!636q39766251:example.org",
                          "state_key": "",
                          "content": {
                            "name": "My Cool Room"
                          },
                          "unsigned": {
                            "age": 1234,
                            "prev_content": {
                              "name": "Old Room Name"
                            },
                            "replaces_state": "$1234prev:example.org"
                          }
                        }
                        """)));
        var response = client.events().getStateEvents(ROOM_ID, EVENT_TYPE, STATE_KEY, Format.CONTENT);
        assertThat(response).isNotNull();
    }

    @Test
    void getMessages_WithValidQueryParameters_thenReturnMessagesResponse() {
        String expectedChunkEventId = "$abcdefg12345:matrix.org";


        QueryParametersMessages mockParams = new QueryParametersMessages("some_start_token", 20, "some_end_token");
        ChronologicalDirection direction = ChronologicalDirection.CHRONOLOGICAL_ORDER; // Adjust to your enum
        // name if needed

        stubFor(get(urlPathEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/messages"))
                .withQueryParam("dir", equalTo("f"))
                .withQueryParam("from", equalTo("some_start_token"))
                .withQueryParam("limit", equalTo("20"))
                .withQueryParam("to", equalTo("some_end_token"))
                .willReturn(okJson("""
                        {
                          "start": "some_start_token",
                          "end": "another_end_token",
                          "chunk": [
                            {
                              "event_id": "%s",
                              "type": "m.room.message",
                              "sender": "@test:matrix.org",
                              "content": { "msgtype": "m.text", "body": "Hello timeline!" }
                            }
                          ]
                        }
                        """.formatted(expectedChunkEventId))));


        Messages actualResponse = client.events().getMessages(ROOM_ID, direction, mockParams);


        assertNotNull(actualResponse, "The returned MessagesResponse payload shouldn't be null");
        assertEquals("some_start_token", actualResponse.start(), "The start pagination token should match");
        assertEquals("another_end_token", actualResponse.end(), "The end pagination token should match");
        assertFalse(actualResponse.chunk().isEmpty(), "The chunked event stream list should contain events");

        // Ensure serialization / list indexing works correctly downstream
        var firstEventId = actualResponse.chunk().getFirst().eventId();
        assertEquals(expectedChunkEventId, firstEventId, "The mapped chunk payload did not match the expected event " +
                "structure");
    }

    @Test
    void getEventCLosestToTimestamp_WithACorrectPayload_ThenReturnAnEventMetadata() {
        ChronologicalDirection chronologicalDirection = ChronologicalDirection.CHRONOLOGICAL_ORDER;
        int randomUnixDate = Math.abs(new Random().nextInt());
        long originServerTs = 1432735824653L;
        stubFor(get(urlPathEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID))
                .withQueryParam("dir", equalTo(chronologicalDirection.getValue()))
                .withQueryParam("ts", equalTo(String.valueOf(randomUnixDate)))
                .willReturn(okJson("""
                        {
                          "event_id": "$143273582443PhrSn:example.org",
                          "origin_server_ts": %d
                        }
                        """.formatted(originServerTs))));
        var response = client.events().getEventClosestToTimestamp(ROOM_ID, chronologicalDirection, randomUnixDate);
        assertThat(response).isNotNull();
        assertThat(response.originServerTs()).isEqualTo(originServerTs);
    }

    @Test
    void getInitialSync_WithACorrectPayload_ThenReturnRoomInfo() {
        stubFor(get("/_matrix/client/v3/rooms/" + ROOM_ID + "/initialSync")
                .willReturn(okJson("""
                        {
                          "account_data": [
                            {
                              "content": {
                                "tags": {
                                  "work": {
                                    "order": "1"
                                  }
                                }
                              },
                              "type": "m.tag"
                            }
                          ],
                          "membership": "join",
                          "messages": {
                            "chunk": [
                              {
                                "content": {
                                  "body": "This is an example text message",
                                  "format": "org.matrix.custom.html",
                                  "formatted_body": "<b>This is an example text message</b>",
                                  "msgtype": "m.text"
                                },
                                "event_id": "$143273582443PhrSn:example.org",
                                "origin_server_ts": 1432735824653,
                                "room_id": "!636q39766251:example.com",
                                "sender": "@example:example.org",
                                "type": "m.room.message",
                                "unsigned": {
                                  "age": 1234,
                                  "membership": "join"
                                }
                              },
                              {
                                "content": {
                                  "body": "something-important.doc",
                                  "filename": "something-important.doc",
                                  "info": {
                                    "mimetype": "application/msword",
                                    "size": 46144
                                  },
                                  "msgtype": "m.file",
                                  "url": "mxc://example.org/FHyPlCeYUSFFxlgbQYZmoEoe"
                                },
                                "event_id": "$143273582443PhrSn:example.org",
                                "origin_server_ts": 1432735824653,
                                "room_id": "!636q39766251:example.com",
                                "sender": "@example:example.org",
                                "type": "m.room.message",
                                "unsigned": {
                                  "age": 1234,
                                  "membership": "join"
                                }
                              }
                            ],
                            "end": "s3456_9_0",
                            "start": "t44-3453_9_0"
                          },
                          "room_id": "!636q39766251:example.com",
                          "state": [
                            {
                              "content": {
                                "join_rule": "public"
                              },
                              "event_id": "$143273582443PhrSn:example.org",
                              "origin_server_ts": 1432735824653,
                              "room_id": "!636q39766251:example.com",
                              "sender": "@example:example.org",
                              "state_key": "",
                              "type": "m.room.join_rules",
                              "unsigned": {
                                "age": 1234,
                                "membership": "join"
                              }
                            },
                            {
                              "content": {
                                "avatar_url": "mxc://example.org/SEsfnsuifSDFSSEF",
                                "displayname": "Alice Margatroid",
                                "membership": "join",
                                "reason": "Looking for support"
                              },
                              "event_id": "$143273582443PhrSn:example.org",
                              "origin_server_ts": 1432735824653,
                              "room_id": "!636q39766251:example.com",
                              "sender": "@alice:example.org",
                              "state_key": "@alice:example.org",
                              "type": "m.room.member",
                              "unsigned": {
                                "age": 1234,
                                "membership": "join"
                              }
                            },
                            {
                              "content": {
                                "m.federate": true,
                                "predecessor": {
                                  "event_id": "$something:example.org",
                                  "room_id": "!oldroom:example.org"
                                },
                                "room_version": "11"
                              },
                              "event_id": "$143273582443PhrSn:example.org",
                              "origin_server_ts": 1432735824653,
                              "room_id": "!636q39766251:example.com",
                              "sender": "@example:example.org",
                              "state_key": "",
                              "type": "m.room.create",
                              "unsigned": {
                                "age": 1234,
                                "membership": "join"
                              }
                            },
                            {
                              "content": {
                                "ban": 50,
                                "events": {
                                  "m.room.name": 100,
                                  "m.room.power_levels": 100
                                },
                                "events_default": 0,
                                "invite": 50,
                                "kick": 50,
                                "notifications": {
                                  "room": 20
                                },
                                "redact": 50,
                                "state_default": 50,
                                "users": {
                                  "@example:localhost": 100
                                },
                                "users_default": 0
                              },
                              "event_id": "$143273582443PhrSn:example.org",
                              "origin_server_ts": 1432735824653,
                              "room_id": "!636q39766251:example.com",
                              "sender": "@example:example.org",
                              "state_key": "",
                              "type": "m.room.power_levels",
                              "unsigned": {
                                "age": 1234,
                                "membership": "join"
                              }
                            }
                          ],
                          "visibility": "private"
                        }
                        """)));
        var response = client.events().getInitialSync(ROOM_ID);
        assertThat(response).isNotNull();
    }

    @Test
    void sendStateEvent_WithACorrectPayload_ThenReturnAString() {
        // TODO pending interface to confirm fields to assert
    }

    @Test
    void sendMessageEvent_WithACorrectPayload_thenReturnAString() {
        String roomMessageType = "m.room.message";
        String expectedEventId = "$h29asdf8q348hju9a:matrix.org";


        stubFor(put(urlEqualTo("/_matrix/client/v3/rooms/" + ROOM_ID + "/send/" + roomMessageType + "/[^/]+"))
                .withRequestBody(equalToJson("""
                        {
                            "body": "Hello World",
                            "msgtype": "m.text"
                        }
                        """, true, true))
                .willReturn(okJson("""
                        {"event_id": "%s"}
                        """.formatted(expectedEventId))));


        RoomMessageEvent textEvent = new Text("Hello World", null, null);
        var actualEventId = client.events().sendMessageEvent(ROOM_ID, textEvent);

        assertNotNull(actualEventId, "The returned event ID should not be null");
        assertEquals(expectedEventId, actualEventId, "The client did not return the expected event ID");
    }

    @Test
    void sendPublishRoomMessageFile_WithACorrectPayload_thenReturnAString(@TempDir Path tempDir) throws IOException {
        Result result = getResult(tempDir);

        // Mock the MXC Request (v1 create endpoint)
        stubFor(post(urlEqualTo("/_matrix/media/v1/create"))
                .willReturn(okJson("{\"content_uri\": \"" + result.mockMxcUri() + "\"}")));

        // Mock the File Upload (v3 upload endpoint with filename query param)
        stubFor(put(urlEqualTo("/_matrix/media/v3/upload/" + result.serverName() + "/" + result.mediaId() + "?filename=file.txt"))
                .withRequestBody(containing("Test"))
                .willReturn(ok()));

        // Mock the Message Publication (v3 client send timeline endpoint)
        stubFor(put(urlPathMatching("/_matrix/client/v3/rooms/" + result.roomId() + "/send/" + result.roomMessageType() + "/[^/]+"))
                .withRequestBody(containing(String.valueOf(result.mockMxcUri)))
                .withRequestBody(containing("file.txt"))
                .willReturn(okJson("{\"event_id\": \"" + result.expectedEventId() + "\"}")));


        var mxc = client.events().uploadResource(result.tempFile);

        File file = new File("Test caption", null, result.tempFile.toString(), null, null, null,
                URI.create(mxc));
        var actualEventId = client.events().sendMessageEvent(result.roomId(), file);

        assertNotNull(actualEventId, "The returned event ID should not be null");
        assertEquals(result.expectedEventId(), actualEventId, "The client did not return the expected event ID");
    }

    @Test
    void sendPublishRoomMessageFile_WithACorrectPayload_thenReturnAnException(@TempDir Path tempDir) throws IOException {
        Result result = getResult(tempDir);

        stubFor(post(urlEqualTo("/_matrix/media/v1/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ malformed json : [")));


        assertThrows(MatrixIOException.class, () -> client.events().uploadResource(result.tempFile));
    }

    @Test
    void getSync_WithValidQueryParameters_thenReturnSyncResponse() {
        String joinedRoomId = "!726s6s6q:example.com";
        String invitedRoomId = "!696r7674:example.com";
        String knockedRoomId = "!223asd456:example.com";
        String leftRoomId = "!left12345:example.com";
        String expectedChunkEventId = "$143273582443PhrSn:example.org";
        String expectedNextBatch = "s72595_4483_1934";

        stubFor(get(urlPathEqualTo("/_matrix/client/v3/sync"))
                .willReturn(okJson("""
                        {
                          "account_data": {
                            "events": [
                              {
                                "content": {
                                  "custom_config_key": "custom_config_value"
                                },
                                "type": "org.example.custom.config"
                              }
                            ]
                          },
                          "next_batch": "s72595_4483_1934",
                          "device_lists": {
                            "changed": ["@alice:matrix.org"],
                            "left": []
                          },
                          "to_device": {
                            "events": [
                              {
                                "content": {
                                  "example_content_key": "value"
                                },
                                "sender": "@alice:example.com",
                                "type": "m.new_device"
                              }
                            ]
                          },
                          "presence": {
                            "events": [
                              {
                                "content": {
                                  "avatar_url": "mxc://localhost/wefuiwegh8742w",
                                  "currently_active": false,
                                  "last_active_ago": 2478593,
                                  "presence": "online",
                                  "status_msg": "Making cupcakes"
                                },
                                "sender": "@example:localhost",
                                "type": "m.presence"
                              }
                            ]
                          },
                          "rooms": {
                            "invite": {
                              "!696r7674:example.com": {
                                "invite_state": {
                                  "events": [
                                    {
                                      "content": {
                                        "name": "My Room Name"
                                      },
                                      "sender": "@alice:example.com",
                                      "state_key": "",
                                      "type": "m.room.name"
                                    },
                                    {
                                      "content": {
                                        "membership": "invite"
                                      },
                                      "sender": "@alice:example.com",
                                      "state_key": "@bob:example.com",
                                      "type": "m.room.member"
                                    }
                                  ]
                                }
                              }
                            },
                            "join": {
                              "!726s6s6q:example.com": {
                                "account_data": {
                                  "events": [
                                    {
                                      "content": {
                                        "tags": {
                                          "u.work": {
                                            "order": 0.9
                                          }
                                        }
                                      },
                                      "type": "m.tag"
                                    },
                                    {
                                      "content": {
                                        "custom_config_key": "custom_config_value"
                                      },
                                      "type": "org.example.custom.room.config"
                                    }
                                  ]
                                },
                                "ephemeral": {
                                  "events": [
                                    {
                                      "content": {
                                        "user_ids": [
                                          "@alice:matrix.org",
                                          "@bob:example.com"
                                        ]
                                      },
                                      "type": "m.typing"
                                    },
                                    {
                                      "content": {
                                        "$1435641916114394fHBLK:matrix.org": {
                                          "m.read": {
                                            "@erikj:jki.re": {
                                              "ts": 1436451550453
                                            }
                                          },
                                          "m.read.private": {
                                            "@self:example.org": {
                                              "ts": 1661384801651
                                            }
                                          }
                                        }
                                      },
                                      "type": "m.receipt"
                                    }
                                  ]
                                },
                                "state": {
                                  "events": [
                                    {
                                      "content": {
                                        "avatar_url": "mxc://example.org/SFHyPlCeYUSFFxlgbQYZmoEoe",
                                        "displayname": "Example user",
                                        "membership": "join"
                                      },
                                      "event_id": "$143273976499sgjks:example.org",
                                      "origin_server_ts": 1432735824653,
                                      "sender": "@example:example.org",
                                      "state_key": "@example:example.org",
                                      "type": "m.room.member",
                                      "unsigned": {
                                        "age": 45603,
                                        "membership": "join"
                                      }
                                    }
                                  ]
                                },
                                "summary": {
                                  "m.heroes": [
                                    "@alice:example.com",
                                    "@bob:example.com"
                                  ],
                                  "m.invited_member_count": 0,
                                  "m.joined_member_count": 2
                                },
                                "timeline": {
                                  "events": [
                                    {
                                      "content": {
                                        "avatar_url": "mxc://example.org/SEsfnsuifSDFSSEF",
                                        "displayname": "Alice Margatroid",
                                        "membership": "join",
                                        "reason": "Looking for support"
                                      },
                                      "event_id": "$143273582443PhrSn:example.org",
                                      "origin_server_ts": 1432735824653,
                                      "sender": "@alice:example.org",
                                      "state_key": "@alice:example.org",
                                      "type": "m.room.member",
                                      "unsigned": {
                                        "age": 1234,
                                        "membership": "join"
                                      }
                                    },
                                    {
                                      "content": {
                                        "body": "This is an example text message",
                                        "format": "org.matrix.custom.html",
                                        "formatted_body": "<b>This is an example text message</b>",
                                        "msgtype": "m.text"
                                      },
                                      "event_id": "$143273582443PhrSn2:example.org",
                                      "origin_server_ts": 1432735824653,
                                      "sender": "@example:example.org",
                                      "type": "m.room.message",
                                      "unsigned": {
                                        "age": 1234,
                                        "membership": "join"
                                      }
                                    }
                                  ],
                                  "limited": true,
                                  "prev_batch": "t34-23535_0_0"
                                },
                                "unread_notifications": {
                                  "highlight_count": 1,
                                  "notification_count": 5
                                },
                                "unread_thread_notifications": {
                                  "$threadroot": {
                                    "highlight_count": 3,
                                    "notification_count": 6
                                  }
                                }
                              }
                            },
                            "knock": {
                              "!223asd456:example.com": {
                                "knock_state": {
                                  "events": [
                                    {
                                      "content": {
                                        "name": "My Room Name"
                                      },
                                      "sender": "@alice:example.com",
                                      "state_key": "",
                                      "type": "m.room.name"
                                    },
                                    {
                                      "content": {
                                        "membership": "knock"
                                      },
                                      "sender": "@bob:example.com",
                                      "state_key": "@bob:example.com",
                                      "type": "m.room.member"
                                    }
                                  ]
                                }
                              }
                            },
                            "leave": {
                              "!left12345:example.com": {
                                "account_data": { "events": [] },
                                "state": { "events": [] },
                                "timeline": { "events": [], "limited": false, "prev_batch": "t00-00000_0_0" }
                              }
                            }
                          }
                        }
                        """)));

        Sync actualResponse = client.events().sync(new QueryParametersSync(null, true, null, null, null, null));

        assertNotNull(actualResponse, "The returned SyncResponse payload shouldn't be null");
        assertEquals(expectedNextBatch, actualResponse.nextBatch(), "The next_batch token should match");

        // rooms.join
        assertTrue(actualResponse.rooms().join().containsKey(joinedRoomId), "Joined rooms should contain the test room");
        var joinedRoom = actualResponse.rooms().join().get(joinedRoomId);
        assertFalse(joinedRoom.timeline().events().isEmpty(), "Joined room timeline should contain events");
        assertEquals(expectedChunkEventId, joinedRoom.timeline().events().getFirst().eventId(),
                "The mapped timeline event did not match the expected event structure");
        assertEquals(2, joinedRoom.summary().mJoinedMemberCount(), "Joined member count should match");
        assertEquals(1, joinedRoom.unreadNotifications().highlightCount(), "Highlight count should match");

        // rooms.invite
        assertTrue(actualResponse.rooms().invite().containsKey(invitedRoomId), "Invited rooms should contain the test room");
        assertFalse(actualResponse.rooms().invite().get(invitedRoomId).inviteState().events().isEmpty(),
                "Invite state should contain stripped state events");

        // rooms.knock
        assertTrue(actualResponse.rooms().knock().containsKey(knockedRoomId), "Knocked rooms should contain the test room");
        assertFalse(actualResponse.rooms().knock().get(knockedRoomId).knockState().events().isEmpty(),
                "Knock state should contain stripped state events");

        // rooms.leave
        assertTrue(actualResponse.rooms().leave().containsKey(leftRoomId), "Left rooms should contain the test room");

        // top-level fields
        assertFalse(actualResponse.accountData().events().isEmpty(), "Account data events should be present");
        assertFalse(actualResponse.presence().events().isEmpty(), "Presence events should be present");
        assertEquals(List.of("@alice:matrix.org"), actualResponse.deviceLists().changed(), "Device list changed should match");
        assertFalse(actualResponse.toDevice().events().isEmpty(), "To-device events should be present");
    }

    private record Result(RoomID roomId, String roomMessageType, String expectedEventId, String serverName,
                          String mediaId, URI mockMxcUri, Path tempFile) {
    }

}