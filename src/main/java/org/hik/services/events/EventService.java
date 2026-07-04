package org.hik.services.events;

import org.hik.api.Event;
import org.hik.api.events.*;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.utils.ConfigurationMapper;
import org.hik.services.utils.HttpTransport;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class EventService implements Event {

    /// Common endpoint for many Room events.
    private static final String ROOM_ENDPOINT = "/_matrix/client/v3/rooms/";
    private final ObjectMapper objectMapper = ConfigurationMapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport(10);

    private final ClientContext context;

    public EventService(ClientContext context) {
        this.context = context;
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
                    httpTransport.putEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() +
                                    ROOM_ENDPOINT + roomId + "/send/m.room.message/" + UUID.randomUUID()),
                            jsonPayload,
                            context.credentials().token());
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
                httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                        "/media/v1/create"), null, this.context.credentials().token());

        JsonNode responsePayload = objectMapper.readTree(queryResponse);
        return responsePayload.get("content_uri").stringValue();
    }

    @Override
    public String uploadResource(Path resource) {
        try {
            String mxc = createAndReserveMXC();

            String rawPath = mxc.replace("mxc://", "");
            URI uploadTargetUri = URI.create(context.discoveryResponse().homeserver().baseUrl() + "/_matrix/media" +
                    "/v3/upload/" + rawPath + "?filename=" + resource.getFileName().toString());
            httpTransport.putResource(uploadTargetUri, resource, context.credentials().token());

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
        String query = httpTransport.buildUrlArgs(context.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                        "/client/v3/sync"
                , args);

        try {
            String queryResponse = httpTransport.getEvent(URI.create(query), context.credentials().token());
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
                httpTransport.buildUrlArgs(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                        "/messages", args);


        try {
            String queryResponse = httpTransport.getEvent(URI.create(finalUrl), context.credentials().token());
            return objectMapper.readValue(queryResponse, Messages.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }


}
