package org.hik.services.events;

import org.hik.api.Event;
import org.hik.api.events.*;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.utils.ConfiguratedMapper;
import org.hik.services.utils.HttpTransport;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventService implements Event {

    /// Common endpoint for many Room events.
    private static final String ROOM_ENDPOINT = "/_matrix/client/v3/rooms/";
    private final ObjectMapper objectMapper = ConfiguratedMapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport();

    private final ClientContext client;

    public EventService(ClientContext client) {
        this.client = client;
    }

    @Override
    public String publishRoomMessage(String roomId, MatrixEvent matrixEvent) {

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
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }


    }

    /// Synchronously creates a new mxc:// for immediate usage.
    ///
    /// @return a [String] representing the MXC
    private String createAndReserveMXC() throws JacksonException {
        String queryResponse =
                httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                        "/media/v1/create"), null, this.client.credentials().token());

        JsonNode responsePayload = objectMapper.readTree(queryResponse);
        return responsePayload.get("content_uri").stringValue();
    }

    @Override
    public String uploadResource(Path resource) {
        try {
            String mxc = createAndReserveMXC();

            String rawPath = mxc.replace("mxc://", "");
            URI uploadTargetUri = URI.create(client.discoveryResponse().homeserver().baseUrl() + "/_matrix/media" +
                    "/v3/upload/" + rawPath + "?filename=" + resource.getFileName().toString());
            httpTransport.putResource(uploadTargetUri, resource, client.credentials().token());

            return mxc;

        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public SyncResponse sync(QueryParametersSync params) {
        Map<String, Object> args = new HashMap<>();
        args.put("filter", params.filter());
        args.put("full_state", String.valueOf(params.fullState()));
        args.put("set_presence", params.setPresence());
        args.put("since", params.since());
        args.put("timeout", String.valueOf(params.timeout()));
        args.put("use_state_after", String.valueOf(params.useStateAfter()));
        String query = this.buildUrlArgs(client.discoveryResponse().homeserver().baseUrl() + "/_matrix/client/v3/sync"
                , args);

        try {
            String queryResponse = httpTransport.getEvent(URI.create(query), client.credentials().token());
            return objectMapper.readValue(queryResponse, SyncResponse.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public Messages getListOfMessages(String roomId, ChronologicalDirectionType dir, QueryParametersMessages params) {
        String payloadRoomId = Objects.requireNonNull(roomId);
        // filter is NOT mapped
        Map<String, Object> args = new HashMap<>();
        args.put("dir", dir.getValue());
        args.put("from", params.from());
        args.put("to", params.to());
        args.put("limit", params.limit());
        String finalUrl =
                this.buildUrlArgs(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                        "/messages", args);


        try {
            String queryResponse = httpTransport.getEvent(URI.create(finalUrl), client.credentials().token());
            return objectMapper.readValue(queryResponse, Messages.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }

    /// Generates an URL GET query with their arguments
    ///
    /// @param basePath the path to insert the parameters
    /// @param params   a list of parameters, null parameters will be ignored.
    /// @return A [String] with the URI to query against
    private String buildUrlArgs(String basePath, Map<String, Object> params) {
        if (params.isEmpty()) return basePath;
        String query = params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(e.getValue().toString(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return basePath + "?" + query;
    }

}
