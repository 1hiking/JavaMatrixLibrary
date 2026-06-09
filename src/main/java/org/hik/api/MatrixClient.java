package org.hik.api;

import org.hik.dtos.payloads.ClientCredentials;
import org.hik.dtos.payloads.MatrixImageMessage;
import org.hik.dtos.payloads.MatrixMessagePayload;
import org.hik.dtos.payloads.MatrixTextMessage;
import org.hik.dtos.responses.MatrixDiscoveryResponse;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import org.hik.networking.CheckResponsePayload;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MatrixClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClientCredentials credentials;
    private final HttpClient client = HttpClient.newHttpClient();
    private String homeserverUrl;

    public MatrixClient(String unprocessedBaseUrl, String username, String authToken) {
        this.credentials = new ClientCredentials(unprocessedBaseUrl, username, authToken);
        this.getWellKnown();

    }

    // TODO for future use
    private void getWellKnown() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(credentials.baseUrl() + "/.well-known/matrix/client"))
                .build();
        CompletableFuture<MatrixDiscoveryResponse> responseFuture = client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(jsonString -> {
                    try {
                        return objectMapper.readValue(jsonString, MatrixDiscoveryResponse.class);
                    } catch (JacksonIOException _) {
                        throw new MatrixIOException("Failed to parse Matrix discovery JSON");
                    }
                });

        MatrixDiscoveryResponse discoveryData = responseFuture.join();
        this.homeserverUrl = discoveryData.homeserver().baseUrl();

    }

    /**
     * @param message A plain message to be sent to the room
     * @param roomId  The id corresponding to a room
     * @return A {@link CompletableFuture} representing a unique identifier of the event
     */
    public CompletableFuture<String> publishRoomMessage(String message, String roomId) {
        String roomMessageType = "m.room.message";
        String eventType = "m.text";

        MatrixMessagePayload payload = new MatrixTextMessage(eventType, message);

        String jsonPayload = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.homeserverUrl + "/_matrix/client/v3/rooms/" + roomId + "/send/" + roomMessageType + "/" + UUID.randomUUID()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.credentials.token())
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();


        return client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(CheckResponsePayload::getStringHttpResponse)
                .thenApply(HttpResponse::body)
                .thenApply(jsonString -> {
                    try {
                        JsonNode responsePayload = objectMapper.readTree(jsonString);
                        return responsePayload.get("event_id").stringValue();
                    } catch (JacksonIOException _) {
                        throw new MatrixIOException("Failed to parse Matrix response JSON");
                    }
                });
    }

    /**
     * @param file   An image file to be sent to the room
     * @param roomId The id corresponding to a room
     * @return A {@link CompletableFuture} representing a unique identifier of the event
     */
    public CompletableFuture<String> publishMultimediaMessage(Path file, String roomId) {
        String roomMessageType = "m.room.message";
        String eventType = "m.image";

        return uploadMultimedia(file).thenCompose(mxcFile -> {
            MatrixMessagePayload matrixMessagePayload = new MatrixImageMessage(eventType, file.getFileName().toString(), mxcFile, null);
            String jsonPayload = objectMapper.writeValueAsString(matrixMessagePayload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(this.homeserverUrl + "/_matrix/client/v3/rooms/" + roomId + "/send/" + roomMessageType + "/" + UUID.randomUUID()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + this.credentials.token())
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            return client
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(CheckResponsePayload::getStringHttpResponse)
                    .thenApply(HttpResponse::body)
                    .thenApply(jsonString -> {
                        try {
                            JsonNode responsePayload = objectMapper.readTree(jsonString);
                            return responsePayload.get("event_id").stringValue();
                        } catch (JacksonIOException _) {
                            throw new MatrixIOException("Failed to parse Matrix response JSON");
                        }
                    });
        });

    }

    /**
     * Asynchronously creates a new mxc:// for immediate usage. This request ignores the unused_expires_at key.
     *
     * @return A {@link String} representing the MXC
     */
    private CompletableFuture<String> createAndReserveMXC() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.homeserverUrl + "/_matrix/media/v1/create"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.credentials.token())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(CheckResponsePayload::getStringHttpResponse)
                .thenApply(HttpResponse::body)
                .thenApply(jsonString -> {
                    try {
                        JsonNode responsePayload = objectMapper.readTree(jsonString);
                        return responsePayload.get("content_uri").stringValue();
                    } catch (JacksonIOException e) {
                        throw new MatrixIOException("Failed to parse Matrix response JSON", e);
                    }
                });
    }

    /**
     * Asynchronously uploads a local multimedia file to the Matrix media server.
     *
     * @param file The local path of the file to upload
     * @return A {@link CompletableFuture} containing the MXC URI string upon successful upload.
     * @throws MatrixIOException      if the local file content cannot be read or probed.
     * @throws MatrixNetworkException via the completed future pipeline if the homeserver rejects the payload.
     */
    private CompletableFuture<String> uploadMultimedia(Path file) {
        // Non-blocking approach: Chain the MXC reservation future seamlessly
        return createAndReserveMXC()
                .thenCompose(mxc -> {
                    String rawPath = mxc.replace("mxc://", "");
                    URI uploadTargetUri = URI.create(this.homeserverUrl
                            + "/_matrix/media/v3/upload/"
                            + rawPath
                            + "?filename=" + file.getFileName().toString());

                    try {
                        HttpRequest uploadRequest = HttpRequest.newBuilder()
                                .uri(uploadTargetUri)
                                .header("Authorization", "Bearer " + this.credentials.token())
                                .header("Content-Type", Files.probeContentType(file))
                                .PUT(HttpRequest.BodyPublishers.ofFile(file))
                                .build();

                        return client.sendAsync(uploadRequest, HttpResponse.BodyHandlers.ofString())
                                .thenApply(CheckResponsePayload::getStringHttpResponse)
                                .thenApply(uploadResponse -> mxc);

                    } catch (IOException e) {
                        return CompletableFuture.failedFuture(new MatrixIOException("Failure to open file content", e));
                    }
                });
    }


}
