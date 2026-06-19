package org.hik.api;

import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import org.hik.payloads.instantmessaging.MatrixEvent;
import org.hik.payloads.roomevents.ChronologicalDirectionEvent;
import org.hik.payloads.roomevents.CreationRoomType;
import org.hik.payloads.roomevents.MatrixRoom;
import org.hik.payloads.roomevents.QueryParametersMessages;
import org.hik.responses.DiscoveryResponse;
import org.hik.responses.MessagesResponse;
import org.hik.services.networking.HttpTransport;
import tools.jackson.core.JacksonException;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link MatrixAPIClient} provides all the functionality required to interact with a Matrix compliant server.
 */
public class MatrixAPIClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClientCredentials credentials;
    private DiscoveryResponse discoveryResponse;
    private final HttpTransport httpTransport = new HttpTransport();


    private MatrixAPIClient(String unprocessedBaseUrl, String username, String authToken) {
        credentials = new ClientCredentials(unprocessedBaseUrl, username, authToken);
    }

    /**
     * Default factory, which will make the initial payloads to request necessary data for further requests
     *
     * @param unprocessedBaseUrl The full qualified url of the server, for example: <a href="https://example.org">https://example.org</a>
     * @param username           The username assigned to a registered account
     * @param authToken          A valid non-expired auth token
     * @return An authenticated client.
     */
    public static CompletableFuture<MatrixAPIClient> createAsync(String unprocessedBaseUrl, String username, String authToken) {
        MatrixAPIClient apiClient = new MatrixAPIClient(unprocessedBaseUrl, username, authToken);
        return apiClient.getWellKnown().thenApply(_ -> apiClient);
    }

    /**
     * Method used to obtain the .well-known data and store the base url.
     *
     * @throws IllegalArgumentException when the homeserver url violates RFC 2396 or is null (since we concat a constant)
     * @throws MatrixIOException        when the payload cannot be processed
     */
    private CompletableFuture<Void> getWellKnown() {

        var query = httpTransport.getJson(URI.create(credentials.baseUrl() + "/.well-known/matrix/client"), null);
        return query.thenCompose(response -> {
            try {
                DiscoveryResponse result = objectMapper.readValue(response, DiscoveryResponse.class);
                return CompletableFuture.completedFuture(result);
            } catch (JacksonException e) {
                return CompletableFuture.failedFuture(new MatrixIOException("Failed to parse Matrix discovery JSON", e.getCause()));
            }
        }).thenAccept(discoveryData -> this.discoveryResponse = discoveryData);
    }


    /**
     *
     * Asynchronously requests the posting of a message to a Matrix room.
     *
     * @param roomId      the id of the room to post the event
     * @param matrixEvent a well constructed {@link MatrixEvent}
     * @return A {@link CompletableFuture} with a {@link String} representing a unique identifier of the event
     * @throws MatrixIOException      when the payload cannot be processed
     * @throws MatrixNetworkException when the response status is not successful
     */
    public CompletableFuture<String> publishRoomMessage(String roomId, MatrixEvent matrixEvent) {

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(matrixEvent);
        } catch (JacksonException e) {
            return CompletableFuture.failedFuture(new MatrixIOException("Failed to parse input data", e));
        }

        var query = httpTransport.putJson(URI.create(
                        this.discoveryResponse.homeserver().baseUrl() + "/_matrix/client/v3/rooms/" + roomId + "/send/m.room.message/" + UUID.randomUUID()),
                jsonPayload,
                this.credentials.token());

        return query.thenCompose(response -> {
            try {
                JsonNode responsePayload = objectMapper.readTree(response);
                JsonNode idNode = responsePayload.path("event_id");
                if (idNode.isMissingNode()) {
                    return CompletableFuture.failedFuture(new MatrixIOException("Missing 'event_id' in server response"));
                }
                return CompletableFuture.completedFuture(idNode.stringValue());
            } catch (JacksonException e) {
                return CompletableFuture.failedFuture(new MatrixIOException("Failed to parse Matrix response JSON", e));
            }
        });

    }

    /**
     *
     * Creates a room, this method will let the homeserver choose the default configuration for most tasks
     * and the following values will overwrite them if set to a non-null value.
     *
     * @param isFederated If the room will be federated
     * @param name        The room's name, if any.
     * @param aliasName   The room's canonical alias, if any
     * @param topic       The room's topic, if any.
     * @param type        The {@link CreationRoomType}
     * @param isVisible   If the room will be visible to the public
     * @return The created room’s ID.
     * @throws MatrixIOException      when the payload cannot be processed
     * @throws MatrixNetworkException when the response status is not successful
     */
    public CompletableFuture<String> createRoom(boolean isFederated, String name, String aliasName, String topic, CreationRoomType type, boolean isVisible) {

        String visibility = isVisible ? "public" : "private";

        MatrixRoom roomPayload = new MatrixRoom(new MatrixRoom.CreationContent(isFederated),
                null,
                null,
                null,
                name,
                type.getValue(),
                aliasName,
                topic,
                visibility);

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(roomPayload);
        } catch (JacksonException e) {
            return CompletableFuture.failedFuture(new MatrixIOException("Failed to parse input data", e));
        }

        var query = httpTransport.postJson(URI.create(this.discoveryResponse.homeserver().baseUrl() + "/_matrix/client/v3/createRoom"),
                jsonPayload, this.credentials.token());

        return query.thenCompose(response -> {
            try {
                JsonNode responsePayload = objectMapper.readTree(response);
                JsonNode idNode = responsePayload.path("room_id");
                if (idNode.isMissingNode()) {
                    return CompletableFuture.failedFuture(new MatrixIOException("Missing 'room_id' in server response"));
                }
                return CompletableFuture.completedFuture(idNode.stringValue());
            } catch (JacksonException e) {
                return CompletableFuture.failedFuture(new MatrixIOException("Failed to parse Matrix response JSON", e));
            }
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


        String finalUrl = getFinalUrl(roomId, dir, params);

        var query = httpTransport.getJson(URI.create(finalUrl), this.credentials.token());

        return query.thenCompose(response -> {
            try {
                return CompletableFuture.completedFuture(objectMapper.readValue(response, MessagesResponse.class));
            } catch (JacksonIOException e) {
                return CompletableFuture.failedFuture(new MatrixIOException("Failed to parse Matrix discovery JSON", e));
            }
        });
    }

    private String getFinalUrl(String roomId, ChronologicalDirectionEvent dir, QueryParametersMessages params) {
        String basePath = this.discoveryResponse.homeserver().baseUrl() + "/_matrix/client/v3/rooms/" + roomId + "/messages";

        StringJoiner queryParams = new StringJoiner("&");
        if (dir != null && dir.getValue() != null) {
            queryParams.add("dir=" + dir.getValue());
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
        return basePath + "?" + queryParams;
    }

    /**
     * Asynchronously creates a new mxc:// for immediate usage. This request ignores the unused_expires_at key.
     *
     * @return A {@link String} representing the MXC
     */
    private CompletableFuture<String> createAndReserveMXC() {
        var query = httpTransport.postJson(URI.create(this.discoveryResponse.homeserver().baseUrl() + "/_matrix/media/v1/create"), null, this.credentials.token());

        return query.thenCompose(response -> {
            try {
                JsonNode responsePayload = objectMapper.readTree(response);
                return CompletableFuture.completedFuture(responsePayload.get("content_uri").stringValue());
            } catch (JacksonException e) {
                return CompletableFuture.failedFuture(new MatrixIOException("Failed to parse Matrix response JSON", e));
            }
        });
    }

    /**
     * Asynchronously uploads a local multimedia resource to the Matrix media server.
     *
     * @param resource The local path of the resource to upload
     * @return A {@link CompletableFuture} containing the MXC URI string upon successful upload.
     * @throws MatrixIOException      if the local resource content cannot be read or probed.
     * @throws MatrixNetworkException via the completed future pipeline if the homeserver rejects the payload.
     */
    public CompletableFuture<String> uploadResource(Path resource) {
        // Non-blocking approach: Chain the MXC reservation future seamlessly
        return createAndReserveMXC().thenCompose(mxc -> {
            String rawPath = mxc.replace("mxc://", "");
            URI uploadTargetUri = URI.create(this.discoveryResponse.homeserver().baseUrl() + "/_matrix/media/v3/upload/" + rawPath + "?filename=" + resource.getFileName().toString());
            try {
                var query = httpTransport.putFile(uploadTargetUri, resource, this.credentials.token());
                return query.thenApply(_ -> mxc);
            } catch (IOException e) {
                return CompletableFuture.failedFuture(new MatrixIOException("Failure to open resource content", e));
            }
        });
    }


}
