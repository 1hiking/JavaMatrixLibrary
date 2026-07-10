package org.hik.services.events;

import org.hik.api.Event;
import org.hik.api.events.*;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.utils.HttpTransport;
import org.hik.services.utils.Mapper;
import org.hik.services.utils.Validator;
import tools.jackson.core.JacksonException;
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
    private final ObjectMapper objectMapper = Mapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport(10);

    private final ClientContext context;

    public EventService(ClientContext context) {
        this.context = context;
    }

    @Override
    public String publishRoomMessage(String roomId, MatrixRoomMessageEvent matrixRoomMessageEvent) {
        roomId = Validator.roomId(roomId);
        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(matrixRoomMessageEvent);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse input data", e);
        }

        URI uri = httpTransport.generateCodifiedURI(context.discoveryResponse().homeserver().baseUrl(),
                ROOM_ENDPOINT + roomId + "/send/m.room.message/" + UUID.randomUUID(), null);
        String queryResponse = httpTransport.putEvent(uri, jsonPayload, context.credentials().token());
        return Mapper.getStringFromSingleObject(queryResponse, "event_id");

    }

    /// Synchronously creates a new mxc:// for immediate usage.
    ///
    /// @return a [String] representing the MXC
    private String createAndReserveMXC() throws JacksonException {
        String queryResponse =
                httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                        "/media/v1/create"), null, this.context.credentials().token());

        return Mapper.getStringFromSingleObject(queryResponse, "content_uri");
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
        URI query = httpTransport.generateCodifiedURI(context.discoveryResponse().homeserver().baseUrl(),
                "/_matrix/client/v3/sync", args);

        String queryResponse = httpTransport.getEvent(query, context.credentials().token());
        return Mapper.getObjectFromString(queryResponse, SyncResponse.class);

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
        URI uri = httpTransport.generateCodifiedURI(context.discoveryResponse().homeserver().baseUrl(),
                ROOM_ENDPOINT + payloadRoomId + "/messages", args);
        String queryResponse = httpTransport.getEvent(uri, context.credentials().token());

        return Mapper.getObjectFromString(queryResponse, Messages.class);
    }


}
