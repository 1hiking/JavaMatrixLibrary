package org.hik.api;

import org.hik.dtos.payloads.ClientCredentials;
import org.hik.dtos.payloads.events.FileMessageTypeEvent;
import org.hik.dtos.payloads.events.MessageTypeEvent;
import org.hik.dtos.payloads.events.QueryParametersMessages;
import org.hik.dtos.payloads.events.TextMessageTypeEvent;
import org.hik.dtos.responses.DiscoveryResponse;
import org.hik.dtos.responses.MessagesResponse;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import org.hik.networking.CheckResponsePayload;
import org.hik.utils.ChronologicalDirectionEvent;
import org.hik.utils.EventType;
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
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link MatrixClient} provides all the functionality required to interact with a Matrix compliant server.
 */
public class MatrixClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClientCredentials credentials;
    private final HttpClient client = HttpClient.newHttpClient();
    private String homeserverUrl;

    /**
     * Default constructor, which will make the initial payloads to request necessary data for further requests
     *
     * @param unprocessedBaseUrl The matrix url of a registered account
     * @param username           The username assigned to a registered account
     * @param authToken          A valid non-expired auth token
     */
    public MatrixClient(String unprocessedBaseUrl, String username, String authToken) {
        this.credentials = new ClientCredentials(unprocessedBaseUrl, username, authToken);
        this.getWellKnown();

    }

    /**
     * Method used to obtain the .well-known data and store the base url.
     */
    private void getWellKnown() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(credentials.baseUrl() + "/.well-known/matrix/client"))
                .build();
        CompletableFuture<DiscoveryResponse> responseFuture = client
                .sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response, DiscoveryResponse.class);
                    } catch (JacksonIOException _) {
                        throw new MatrixIOException("Failed to parse Matrix discovery JSON");
                    }
                });

        DiscoveryResponse discoveryData = responseFuture.join();
        this.homeserverUrl = discoveryData.homeserver().baseUrl();

    }

    /**
     *
     * Asynchronously posts a message to a Matrix room.
     *
     * @param message The message to be sent to the room
     * @param roomId  The id corresponding to a room
     * @return A {@link CompletableFuture} with a {@link String} representing a unique identifier of the event
     */
    public CompletableFuture<String> publishRoomMessage(String message, String roomId) {
        String roomMessageType = "m.room.message";
        String eventType = "m.text";

        MessageTypeEvent payload = new TextMessageTypeEvent(eventType, message);

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
                .thenApply(response -> {
                    try {
                        JsonNode responsePayload = objectMapper.readTree(response);
                        return responsePayload.get("event_id").stringValue();
                    } catch (JacksonIOException _) {
                        throw new MatrixIOException("Failed to parse Matrix response JSON");
                    }
                });
    }

    /**
     *
     * Asynchronously posts a file to a Matrix room.
     *
     * @param file      The file to be sent to the room
     * @param roomId    The id corresponding to a room
     * @param eventType The {@link EventType} corresponding to what's being uploaded
     * @return A {@link CompletableFuture} with a {@link String} representing a unique identifier of the event
     * @throws MatrixIOException      when the payload cannot be processed
     * @throws MatrixNetworkException when the response is not successful
     */
    public CompletableFuture<String> publishRoomMessage(Path file, String roomId, EventType eventType) {
        String roomMessageType = "m.room.message";

        return uploadMultimedia(file).thenCompose(mxcFile -> {
            MessageTypeEvent messageTypeEvent = new FileMessageTypeEvent(eventType.type, file.getFileName().toString(), mxcFile, null);
            String jsonPayload = objectMapper.writeValueAsString(messageTypeEvent);
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
                    .thenApply(response -> {
                        try {
                            JsonNode responsePayload = objectMapper.readTree(response);
                            return responsePayload.get("event_id").stringValue();
                        } catch (JacksonIOException _) {
                            throw new MatrixIOException("Failed to parse Matrix response JSON");
                        }
                    });
        });

    }


    /**
     *
     * Method that asynchronously returns a list of message and state events for a room. It uses pagination query parameters to paginate history in the room.
     * <p>
     * The content is not parsed or escaped which means newlines (\n) and such sequences will be treated as they are.
     *
     * @param roomId The room to get events from.
     * @param params The {@link QueryParametersMessages} for the operation
     * @param dir    The {@link ChronologicalDirectionEvent} to return events from.
     * @return A {@link CompletableFuture} containing a {@link MessagesResponse} with the messages from the room
     */
    public CompletableFuture<MessagesResponse> getListOfMessages(String roomId, ChronologicalDirectionEvent dir, QueryParametersMessages params) {


        String basePath = this.homeserverUrl + "/_matrix/client/v3/rooms/" + roomId + "/messages";

        StringJoiner queryParams = new StringJoiner("&");
        if (dir != null && dir.order != null) {
            queryParams.add("dir=" + dir.order);
        }
        if (params.from() != null) {
            queryParams.add("from=" + params.from());
        }
        if (params.limit() > 0) {
            queryParams.add("limit=" + params.limit());
        }
        if (params.to() != null) {
            queryParams.add("to=" + params.to());
        }
        String finalUrl = basePath + "?" + queryParams;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + this.credentials.token())
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(CheckResponsePayload::getStringHttpResponse)
                .thenApply(HttpResponse::body)
                .thenApply(response -> {
                    try {
                        return objectMapper.readValue(response, MessagesResponse.class);
                    } catch (JacksonIOException _) {
                        throw new MatrixIOException("Failed to parse Matrix discovery JSON");
                    }
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
                .thenApply(response -> {
                    try {
                        JsonNode responsePayload = objectMapper.readTree(response);
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
