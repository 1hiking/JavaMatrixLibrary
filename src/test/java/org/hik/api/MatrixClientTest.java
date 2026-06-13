package org.hik.api;


import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.hik.dtos.payloads.events.QueryParametersMessages;
import org.hik.dtos.responses.MessagesResponse;
import org.hik.exceptions.MatrixIOException;
import org.hik.utils.ChronologicalDirectionEvent;
import org.hik.utils.EventType;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class MatrixClientTest {
    private MatrixClient matrixClient;
    private static final String USER = "test";
    private static final String AUTH_TOKEN = "1234";


    @RegisterExtension
    static final WireMockExtension wireMockServer = WireMockExtension.newInstance()
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

    // Initialization tests

    @Test
    void getWellKnown_WithAllRequiredProperties_thenReturnCorrectSerialization() {
        matrixClient = new MatrixClient(wireMockServer.baseUrl(), USER, AUTH_TOKEN);

        assertDoesNotThrow(() -> matrixClient, "The client should not throw given a good url.");
    }

    @Test
    void getWellKnown_WithBadUrl_thenReturnAnException() {
        assertThrows(IllegalArgumentException.class, () -> new MatrixClient("INCORRECT.ORG", USER, AUTH_TOKEN), "The client should throw when given a bad url.");
    }


    // Publishing messages
    @Test
    void sendPublishRoomMessage_WithACorrectPayload_thenReturnAString() {
        String roomId = "1234";
        String roomMessageType = "m.room.message";
        String expectedEventId = "$h29asdf8q348hju9a:matrix.org";


        wireMockServer.stubFor(put(urlPathMatching("/_matrix/client/v3/rooms/" + roomId + "/send/" + roomMessageType + "/[^/]+"))
                .withRequestBody(containing("Hello World"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"event_id\": \"" + expectedEventId + "\"}")));

        matrixClient = new MatrixClient(wireMockServer.baseUrl(), USER, AUTH_TOKEN);

        String actualEventId = matrixClient.publishRoomMessage("Hello World", roomId).join();

        assertNotNull(actualEventId, "The returned event ID should not be null");
        assertEquals(expectedEventId, actualEventId, "The client did not return the expected event ID");
    }


    @Test
    void sendPublishRoomMessageFile_WithACorrectPayload_thenReturnAString(@TempDir Path tempDir) throws IOException {
        // Arrange
        Result result = getResult(tempDir);

        // Mock the MXC Request (v1 create endpoint)
        wireMockServer.stubFor(post(urlEqualTo("/_matrix/media/v1/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"content_uri\": \"" + result.mockMxcUri() + "\"}")));

        // Mock the File Upload (v3 upload endpoint with filename query param)
        wireMockServer.stubFor(put(urlEqualTo("/_matrix/media/v3/upload/" + result.serverName() + "/" + result.mediaId() + "?filename=file.txt"))
                .withRequestBody(containing("Test"))
                .willReturn(aResponse()
                        .withStatus(200)));

        // Mock the Message Publication (v3 client send timeline endpoint)
        wireMockServer.stubFor(put(urlPathMatching("/_matrix/client/v3/rooms/" + result.roomId() + "/send/" + result.roomMessageType() + "/[^/]+"))
                .withRequestBody(containing(result.mockMxcUri()))
                .withRequestBody(containing("file.txt"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"event_id\": \"" + result.expectedEventId() + "\"}")));

        // Initialize client pointing to local WireMock server
        matrixClient = new MatrixClient(wireMockServer.baseUrl(), USER, AUTH_TOKEN);

        // Act
        String actualEventId = matrixClient.publishRoomMessage(result.tempFile(), result.roomId(), EventType.FILE).join();

        // Assert
        assertNotNull(actualEventId, "The returned event ID should not be null");
        assertEquals(result.expectedEventId(), actualEventId, "The client did not return the expected event ID");
    }

    private static @NonNull Result getResult(Path tempDir) throws IOException {
        String roomId = "1234";
        String roomMessageType = "m.room.message";
        String expectedEventId = "$h29asdf8q348hju9a:matrix.org";

        String serverName = "matrix.org";
        String mediaId = "fakeMediaId123";
        String mockMxcUri = "mxc://" + serverName + "/" + mediaId;

        Path tempFile = tempDir.resolve("file.txt");
        Files.writeString(tempFile, "Test");
        return new Result(roomId, roomMessageType, expectedEventId, serverName, mediaId, mockMxcUri, tempFile);
    }

    private record Result(String roomId, String roomMessageType, String expectedEventId, String serverName,
                          String mediaId, String mockMxcUri, Path tempFile) {
    }

    @Test
    void sendPublishRoomMessageFile_WithACorrectPayload_thenReturnAnException(@TempDir Path tempDir) throws IOException {
        Result result = getResult(tempDir);

        wireMockServer.stubFor(post(urlEqualTo("/_matrix/media/v1/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ malformed json : [")));

        matrixClient = new MatrixClient(wireMockServer.baseUrl(), USER, AUTH_TOKEN);

        var actualEventId = matrixClient.publishRoomMessage(result.tempFile(), result.roomId(), EventType.FILE);

        // Capture the asynchronous CompletionException wrapper
        var discard = assertThrows(
                java.util.concurrent.CompletionException.class,
                actualEventId::join
        );

        // Use regular AssertJ on the true inner cause
        assertThat(discard.getCause())
                .isInstanceOf(MatrixIOException.class)
                .hasMessageContaining("Failed to parse Matrix response JSON");
    }

    // Pagination & Timeline History Tests

    @Test
    void getListOfMessages_WithValidQueryParameters_thenReturnMessagesResponse() {
        String roomId = "!exampleRoomId:matrix.org";
        String expectedChunkEventId = "$abcdefg12345:matrix.org";


        QueryParametersMessages mockParams = new QueryParametersMessages("some_start_token", 20, "some_end_token");
        org.hik.utils.ChronologicalDirectionEvent direction = ChronologicalDirectionEvent.CHRONOLOGICAL_ORDER; // Adjust to your enum name if needed

        wireMockServer.stubFor(get(urlPathEqualTo("/_matrix/client/v3/rooms/" + roomId + "/messages"))
                .withQueryParam("dir", equalTo("f"))
                .withQueryParam("from", equalTo("some_start_token"))
                .withQueryParam("limit", equalTo("20"))
                .withQueryParam("to", equalTo("some_end_token"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
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

        matrixClient = new MatrixClient(wireMockServer.baseUrl(), USER, AUTH_TOKEN);

        MessagesResponse actualResponse = matrixClient.getListOfMessages(roomId, direction, mockParams).join();

        assertNotNull(actualResponse, "The returned MessagesResponse payload shouldn't be null");
        assertEquals("some_start_token", actualResponse.start(), "The start pagination token should match");
        assertEquals("another_end_token", actualResponse.end(), "The end pagination token should match");
        assertFalse(actualResponse.chunk().isEmpty(), "The chunked event stream list should contain events");

        // Ensure serialization / list indexing works correctly downstream
        var firstEventId = actualResponse.chunk().getFirst().eventId();
        assertEquals(expectedChunkEventId, firstEventId, "The mapped chunk payload did not match the expected event structure");
    }
}