package org.hik.services.modules;

import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import org.hik.payloads.roomevents.MatrixEvent;
import org.hik.payloads.roomstate.ChronologicalDirectionType;
import org.hik.payloads.roomstate.Messages;
import org.hik.payloads.roomstate.QueryParametersMessages;
import org.hik.services.networking.HttpTransport;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Events {

    /**
     * Common endpoint for many Room events.
     */
    private static final String ROOM_ENDPOINT = "/_matrix/client/v3/rooms/";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpTransport httpTransport = new HttpTransport();

    private final ClientContext client;

    public Events(ClientContext client) {
        this.client = client;
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
            String queryResponse =
                    httpTransport.putEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() +
                                    ROOM_ENDPOINT + roomId + "/send/m.room.message/" + UUID.randomUUID()),
                            jsonPayload,
                            client.credentials().token());
            JsonNode responsePayload = objectMapper.readTree(queryResponse);
            JsonNode idNode = responsePayload.path("event_id");
            if (idNode.isMissingNode()) {
                throw new MatrixIOException("Missing 'event_id' in server response ");
            }
            return idNode.stringValue();
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to publish an event ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }


    }

    /// Synchronously creates a new mxc:// for immediate usage.
    ///
    /// @return a [String] representing the MXC
    private String createAndReserveMXC() throws JacksonException, IOException, InterruptedException {
        var queryResponse =
                httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                        "/media/v1/create"), null, this.client.credentials().token());

        JsonNode responsePayload = objectMapper.readTree(queryResponse);
        return responsePayload.get("content_uri").stringValue();
    }

    /// Synchronously uploads a local multimedia resource to the Matrix media server.
    ///
    /// @param resource the local path of the resource to upload
    /// @return A [String] containing the MXC URI string upon successful upload.
    /// @throws InterruptedException when the HTTP Client is interrupted
    public String uploadResource(Path resource) throws InterruptedException {
        try {
            String mxc = createAndReserveMXC();

            String rawPath = mxc.replace("mxc://", "");
            URI uploadTargetUri = URI.create(client.discoveryResponse().homeserver().baseUrl() + "/_matrix/media" +
                    "/v3/upload/" + rawPath + "?filename=" + resource.getFileName().toString());
            httpTransport.putResource(uploadTargetUri, resource, client.credentials().token());

            return mxc;

        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to publish a resource ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    /// Returns a list of message and state events for a room. It uses pagination query parameters to paginate
    /// history in the room.
    /// The content is not parsed or escaped which means newlines (\n) and such escape sequences will not be parsed.
    ///
    /// @param roomId the target room ID.
    /// @param params the [QueryParametersMessages] for the operation
    /// @param dir    the [ChronologicalDirectionType] to return events from.
    /// @return A [CompletableFuture] containing a [Messages] record with the messages from the room
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted
    /// @throws NullPointerException when the roomId is null.
    public Messages getListOfMessages(String roomId, ChronologicalDirectionType dir, QueryParametersMessages params) throws InterruptedException {
        var payloadRoomId = Objects.requireNonNull(roomId);
        // filter is NOT mapped
        Map<String, String> args = Map.ofEntries(
                Map.entry("dir", dir.getValue()),
                Map.entry("from", params.from()),
                Map.entry("to", params.to()),
                Map.entry("limit", String.valueOf(params.limit())));
        String finalUrl =
                this.buildUrlArgs(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                        "/messages", args);


        try {
            var queryResponse = httpTransport.getEvent(URI.create(finalUrl), client.credentials().token());
            return objectMapper.readValue(queryResponse, Messages.class);
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to fetch messages ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }

    /// Generates an URL GET query with their arguments
    ///
    /// @param basePath the path to insert the parameters
    /// @param params   a list of parameters, null parameters will be ignored.
    /// @return A [String] with the URI to query against
    private String buildUrlArgs(String basePath, Map<String, String> params) {
        if (params.isEmpty()) return basePath;
        String query = params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return basePath + "?" + query;
    }

}
