package org.hik.services.modules;

import org.hik.api.Event;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.payloads.roomevents.ChronologicalDirectionType;
import org.hik.payloads.roomevents.MatrixEvent;
import org.hik.payloads.roomevents.Messages;
import org.hik.payloads.roomevents.QueryParametersMessages;
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
import java.util.stream.Collectors;

public class EventService implements Event {

    /**
     * Common endpoint for many Room events.
     */
    private static final String ROOM_ENDPOINT = "/_matrix/client/v3/rooms/";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpTransport httpTransport = new HttpTransport();

    private final ClientContext client;

    public EventService(ClientContext client) {
        this.client = client;
    }

    @Override
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

    @Override
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

    @Override
    public void doSync() throws InterruptedException {
        try {
            httpTransport.getEvent(URI.create("/_matrix/client/v3/sync"), this.client.credentials().token());
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to publish a resource ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
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
