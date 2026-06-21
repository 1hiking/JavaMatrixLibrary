package org.hik.api;

import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import org.hik.payloads.roomevents.MatrixEvent;
import org.hik.payloads.roomstate.ChronologicalDirectionEvent;
import org.hik.payloads.roomstate.CreationRoomType;
import org.hik.payloads.roomstate.MatrixRoom;
import org.hik.payloads.roomstate.QueryParametersMessages;
import org.hik.responses.DiscoveryResponse;
import org.hik.responses.MessagesResponse;
import org.hik.services.networking.HttpTransport;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/// A [MatrixClient] provides all the functionality required to interact with a Matrix compliant server.
public class MatrixClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ClientCredentials credentials;
    private final HttpTransport httpTransport = new HttpTransport();
    private DiscoveryResponse discoveryResponse;


    private MatrixClient(String unprocessedBaseUrl, String username, String authToken) {
        credentials = new ClientCredentials(unprocessedBaseUrl, username, authToken);
    }

    /// Default factory, which will make the initial payloads to request necessary data for further requests
    ///
    /// @param unprocessedBaseUrl The full qualified url of the server, for example: [https://example.org](https://example.org)
    /// @param username           The username assigned to a registered account
    /// @param authToken          A valid non-expired auth token
    /// @return An authenticated client.
    /// @throws InterruptedException when the HTTP Client is interrupted
    public static MatrixClient create(String unprocessedBaseUrl, String username, String authToken) throws InterruptedException {
        MatrixClient apiClient = new MatrixClient(unprocessedBaseUrl, username, authToken);
        apiClient.getWellKnown();
        return apiClient;
    }

    /// Method used to obtain the .well-known data and store the base url.º
    ///
    /// @throws IllegalArgumentException when the homeserver url violates RFC 2396 or is null (since we concat a constant)
    /// @throws MatrixIOException        when the payload cannot be processed
    /// @throws InterruptedException     when the HTTP Client is interrupted
    private void getWellKnown() throws InterruptedException {
        try {
            URI uri = URI.create(credentials.baseUrl() + "/.well-known/matrix/client");
            var response = httpTransport.getJson(uri, null);
            this.discoveryResponse = objectMapper.readValue(response, DiscoveryResponse.class);

        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix discovery JSON ", e);
        } catch (IOException e) {
            throw new MatrixIOException("Network error during Matrix discovery ", e);
        }
    }


    /// Asynchronously requests the posting of a message to a Matrix room.
    ///
    /// @param roomId      the id of the room to post the event
    /// @param matrixEvent a well constructed [MatrixEvent]
    /// @return A [CompletableFuture] with a [String] representing a unique identifier of the event
    /// @throws MatrixIOException      when the payload cannot be processed
    /// @throws MatrixNetworkException when the response status is not successful
    /// @throws InterruptedException   when the HTTP Client is interrupted
    public String publishRoomMessage(String roomId, MatrixEvent matrixEvent) throws InterruptedException {

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(matrixEvent);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse input data", e);
        }

        try {
            String queryResponse = httpTransport.putJson(URI.create(this.discoveryResponse.homeserver().baseUrl() + "/_matrix/client/v3/rooms/" + roomId + "/send/m.room.message/" + UUID.randomUUID()),
                    jsonPayload,
                    this.credentials.token());
            JsonNode responsePayload = objectMapper.readTree(queryResponse);
            JsonNode idNode = responsePayload.path("event_id");
            if (idNode.isMissingNode()) {
                throw new MatrixIOException("Missing 'event_id' in server response ");
            }
            return idNode.stringValue();
        } catch (IOException e) {
            throw new MatrixIOException("Network error during Matrix message publishing ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }


    }

    /// Creates a room, this method will let the homeserver choose the default configuration for most tasks
    /// and the following values will overwrite them if set to a non-null value.
    ///
    /// @param isFederated If the room will be federated
    /// @param name        The room's name, if any.
    /// @param aliasName   The room's canonical alias, if any
    /// @param topic       The room's topic, if any.
    /// @param type        The [CreationRoomType]
    /// @param isVisible   If the room will be visible to the public
    /// @return The created room’s ID.
    /// @throws MatrixIOException      when the payload cannot be processed
    /// @throws MatrixNetworkException when the response status is not successful
    /// @throws InterruptedException   when the HTTP Client is interrupted
    public String createRoom(boolean isFederated, String name, String aliasName, String topic, CreationRoomType type, boolean isVisible) throws InterruptedException {

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
            throw new MatrixIOException("Failed to parse input data", e);
        }

        String queryResponse = null;
        try {
            queryResponse = httpTransport.postJson(URI.create(this.discoveryResponse.homeserver().baseUrl() + "/_matrix/client/v3/createRoom"),
                    jsonPayload, this.credentials.token());

            JsonNode responsePayload = objectMapper.readTree(queryResponse);
            JsonNode idNode = responsePayload.path("room_id");
            if (idNode.isMissingNode()) {
                throw new MatrixIOException("Missing 'room_id' in server response ");
            }
            return idNode.stringValue();
        } catch (IOException e) {
            throw new MatrixIOException("Network error during Matrix room creation ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }


    /// Method that asynchronously returns a list of message and state events for a room. It uses pagination query parameters to paginate history in the room.
    ///
    /// The content is not parsed or escaped which means newlines (\n) and such sequences will be treated as they are.
    ///
    /// @param roomId The room to get events from.
    /// @param params The [QueryParametersMessages] for the operation
    /// @param dir    The [ChronologicalDirectionEvent] to return events from.
    /// @return A [CompletableFuture] containing a [MessagesResponse] with the messages from the room
    /// @throws InterruptedException when the HTTP Client is interrupted
    public MessagesResponse getListOfMessages(String roomId, ChronologicalDirectionEvent dir, QueryParametersMessages params) throws InterruptedException {


        String finalUrl = getFinalUrl(roomId, dir, params);

        try {
            var queryResponse = httpTransport.getJson(URI.create(finalUrl), this.credentials.token());
            return objectMapper.readValue(queryResponse, MessagesResponse.class);
        } catch (IOException e) {
            throw new MatrixIOException("Network error during Matrix message fetching ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

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

    /// Synchronously creates a new mxc:// for immediate usage.
    ///
    /// @return A [String] representing the MXC
    /// @throws InterruptedException when the HTTP Client is interrupted
    private String createAndReserveMXC() throws InterruptedException {
        try {
            var queryResponse = httpTransport.postJson(URI.create(this.discoveryResponse.homeserver().baseUrl() + "/_matrix/media/v1/create"), null, this.credentials.token());
            JsonNode responsePayload = objectMapper.readTree(queryResponse);
            return responsePayload.get("content_uri").stringValue();
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        } catch (IOException e) {
            throw new MatrixIOException("Matrix communications or parsing failed", e);
        }
    }

    /// Synchronously uploads a local multimedia resource to the Matrix media server.
    ///
    /// @param resource The local path of the resource to upload
    /// @return A [String] containing the MXC URI string upon successful upload.
    /// @throws InterruptedException when the HTTP Client is interrupted
    public String uploadResource(Path resource) throws InterruptedException {
        try {
            String mxc = createAndReserveMXC();

            String rawPath = mxc.replace("mxc://", "");
            URI uploadTargetUri = URI.create(this.discoveryResponse.homeserver().baseUrl() + "/_matrix/media/v3/upload/" + rawPath + "?filename=" + resource.getFileName().toString());
            httpTransport.putFile(uploadTargetUri, resource, this.credentials.token());

            return mxc;
        } catch (IOException e) {
            throw new MatrixIOException("Network error during Matrix resource publishing", e);
        }
    }
}
