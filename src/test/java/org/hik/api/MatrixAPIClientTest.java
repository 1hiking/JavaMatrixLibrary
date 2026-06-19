package org.hik.api;


import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.hik.exceptions.MatrixIOException;
import org.hik.payloads.instantmessaging.MatrixEvent;
import org.hik.payloads.instantmessaging.MatrixFile;
import org.hik.payloads.instantmessaging.MatrixText;
import org.hik.payloads.roomevents.ChronologicalDirectionEvent;
import org.hik.payloads.roomevents.QueryParametersMessages;
import org.hik.responses.MessagesResponse;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class MatrixAPIClientTest {
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
        var client = MatrixAPIClient.createAsync(wireMockServer.baseUrl(), USER, AUTH_TOKEN);

        assertDoesNotThrow(() -> client, "The client should not throw given a good url.");
    }

    @Test
    void getWellKnown_WithBadUrl_thenReturnAnException() {
        assertThrows(IllegalArgumentException.class, () -> MatrixAPIClient.createAsync("INCORRECT.ORG", USER, AUTH_TOKEN), "The client should throw when given a bad url.");
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

        var eventIdFuture = MatrixAPIClient.createAsync(wireMockServer.baseUrl(), USER, AUTH_TOKEN)
                .thenCompose(client -> {
                    MatrixEvent textEvent = new MatrixText("Hello World");
                    return client.publishRoomMessage(roomId, textEvent);
                });

        String actualEventId = eventIdFuture.join();


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

        var eventIdFuture = MatrixAPIClient.createAsync(wireMockServer.baseUrl(), USER, AUTH_TOKEN)
                .thenCompose(matrixAPIClient1 -> matrixAPIClient1.uploadResource(result.tempFile).thenCompose(mxc -> {
                    MatrixFile file = new MatrixFile("Test caption", result.tempFile.toString(), URI.create(mxc));
                    return matrixAPIClient1.publishRoomMessage(result.roomId(), file);
                }));

        // Act
        String actualEventId = eventIdFuture.join();

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

        var eventIdFuture = MatrixAPIClient.createAsync(wireMockServer.baseUrl(), USER, AUTH_TOKEN)
                .thenCompose(matrixAPIClient1 -> matrixAPIClient1.uploadResource(result.tempFile).thenCompose(mxc -> {
                    MatrixFile file = new MatrixFile("Test caption", result.tempFile.toString(), URI.create(mxc));
                    return matrixAPIClient1.publishRoomMessage(result.roomId(), file);
                }));


        // Capture the asynchronous CompletionException wrapper
        var discard = assertThrows(
                java.util.concurrent.CompletionException.class,
                eventIdFuture::join
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
        ChronologicalDirectionEvent direction = ChronologicalDirectionEvent.CHRONOLOGICAL_ORDER; // Adjust to your enum name if needed

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


        var expectedResponse = MatrixAPIClient.createAsync(wireMockServer.baseUrl(), USER, AUTH_TOKEN)
                .thenCompose(matrixAPIClient1 -> matrixAPIClient1.getListOfMessages(roomId, direction, mockParams));

        MessagesResponse actualResponse = expectedResponse.join();

        assertNotNull(actualResponse, "The returned MessagesResponse payload shouldn't be null");
        assertEquals("some_start_token", actualResponse.start(), "The start pagination token should match");
        assertEquals("another_end_token", actualResponse.end(), "The end pagination token should match");
        assertFalse(actualResponse.chunk().isEmpty(), "The chunked event stream list should contain events");

        // Ensure serialization / list indexing works correctly downstream
        var firstEventId = actualResponse.chunk().getFirst().eventId();
        assertEquals(expectedChunkEventId, firstEventId, "The mapped chunk payload did not match the expected event structure");
    }
}