package org.hik.services.modules;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.hik.api.MatrixAPIClientTest;
import org.hik.api.MatrixClient;
import org.hik.exceptions.MatrixIOException;
import org.hik.payloads.roomevents.*;
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
import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest extends MatrixAPIClientTest {

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
                .willReturn(okJson("{\"m.homeserver\": {\"base_url\": \"" + wireMockServer.baseUrl() + "\"}}")));


    }


    @Test
    void sendPublishRoomMessage_WithACorrectPayload_thenReturnAString() throws InterruptedException {
        String roomId = "1234";
        String roomMessageType = "m.room.message";
        String expectedEventId = "$h29asdf8q348hju9a:matrix.org";


        wireMockServer.stubFor(put(urlPathMatching("/_matrix/client/v3/rooms/" + roomId + "/send/" + roomMessageType + "/[^/]+"))
                .withRequestBody(equalToJson("""
                        {
                            "body": "Hello World",
                            "msgtype": "m.text"
                        }
                        """, true, true))
                .willReturn(okJson("{\"event_id\": \"" + expectedEventId + "\"}")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);

        MatrixEvent textEvent = new MatrixText("Hello World", null, null);
        var actualEventId = client.events().publishRoomMessage(roomId, textEvent);


        assertNotNull(actualEventId, "The returned event ID should not be null");
        assertEquals(expectedEventId, actualEventId, "The client did not return the expected event ID");
    }


    @Test
    void sendPublishRoomMessageFile_WithACorrectPayload_thenReturnAString(@TempDir Path tempDir) throws IOException,
            InterruptedException {
        Result result = getResult(tempDir);

        // Mock the MXC Request (v1 create endpoint)
        wireMockServer.stubFor(post(urlEqualTo("/_matrix/media/v1/create"))
                .willReturn(okJson("{\"content_uri\": \"" + result.mockMxcUri() + "\"}")));

        // Mock the File Upload (v3 upload endpoint with filename query param)
        wireMockServer.stubFor(put(urlEqualTo("/_matrix/media/v3/upload/" + result.serverName() + "/" + result.mediaId() + "?filename=file.txt"))
                .withRequestBody(containing("Test"))
                .willReturn(ok()));

        // Mock the Message Publication (v3 client send timeline endpoint)
        wireMockServer.stubFor(put(urlPathMatching("/_matrix/client/v3/rooms/" + result.roomId() + "/send/" + result.roomMessageType() + "/[^/]+"))
                .withRequestBody(containing(String.valueOf(result.mockMxcUri)))
                .withRequestBody(containing("file.txt"))
                .willReturn(okJson("{\"event_id\": \"" + result.expectedEventId() + "\"}")));


        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);
        var mxc = client.events().uploadResource(result.tempFile);

        MatrixFile file = new MatrixFile("Test caption", null, result.tempFile.toString(), null, null, null,
                URI.create(mxc));
        var actualEventId = client.events().publishRoomMessage(result.roomId(), file);

        assertNotNull(actualEventId, "The returned event ID should not be null");
        assertEquals(result.expectedEventId(), actualEventId, "The client did not return the expected event ID");
    }

    private static @NonNull Result getResult(Path tempDir) throws IOException {
        String roomId = "1234";
        String roomMessageType = "m.room.message";
        String expectedEventId = "$h29asdf8q348hju9a:matrix.org";

        String serverName = "matrix.org";
        String mediaId = "fakeMediaId123";
        URI mockMxcUri = URI.create("mxc://" + serverName + "/" + mediaId);

        Path tempFile = tempDir.resolve("file.txt");
        Files.writeString(tempFile, "Test");
        return new Result(roomId, roomMessageType, expectedEventId, serverName, mediaId, mockMxcUri, tempFile);
    }

    private record Result(String roomId, String roomMessageType, String expectedEventId, String serverName,
                          String mediaId, URI mockMxcUri, Path tempFile) {
    }

    @Test
    void sendPublishRoomMessageFile_WithACorrectPayload_thenReturnAnException(@TempDir Path tempDir) throws IOException, InterruptedException {
        Result result = getResult(tempDir);

        wireMockServer.stubFor(post(urlEqualTo("/_matrix/media/v1/create"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ malformed json : [")));

        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);


        assertThrows(MatrixIOException.class, () -> client.events().uploadResource(result.tempFile));
    }

    @Test
    void getListOfMessages_WithValidQueryParameters_thenReturnMessagesResponse() throws InterruptedException {
        String roomId = "!exampleRoomId:matrix.org";
        String expectedChunkEventId = "$abcdefg12345:matrix.org";


        QueryParametersMessages mockParams = new QueryParametersMessages("some_start_token", 20, "some_end_token");
        ChronologicalDirectionType direction = ChronologicalDirectionType.CHRONOLOGICAL_ORDER; // Adjust to your enum
        // name if needed

        wireMockServer.stubFor(get(urlPathEqualTo("/_matrix/client/v3/rooms/" + roomId + "/messages"))
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


        var client = MatrixClient.create(wireMockServer.baseUrl(), USER, AUTH_TOKEN);

        Messages actualResponse = client.events().getListOfMessages(roomId, direction, mockParams);


        assertNotNull(actualResponse, "The returned MessagesResponse payload shouldn't be null");
        assertEquals("some_start_token", actualResponse.start(), "The start pagination token should match");
        assertEquals("another_end_token", actualResponse.end(), "The end pagination token should match");
        assertFalse(actualResponse.chunk().isEmpty(), "The chunked event stream list should contain events");

        // Ensure serialization / list indexing works correctly downstream
        var firstEventId = actualResponse.chunk().getFirst().eventId();
        assertEquals(expectedChunkEventId, firstEventId, "The mapped chunk payload did not match the expected event " +
                "structure");
    }

}