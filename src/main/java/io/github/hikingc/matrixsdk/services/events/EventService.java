package io.github.hikingc.matrixsdk.services.events;

import io.github.hikingc.matrixsdk.api.Event;
import io.github.hikingc.matrixsdk.api.events.*;
import io.github.hikingc.matrixsdk.api.identifiers.RoomID;
import io.github.hikingc.matrixsdk.context.ClientContext;
import io.github.hikingc.matrixsdk.exceptions.MatrixIOException;
import io.github.hikingc.matrixsdk.services.utils.HttpTransport;
import io.github.hikingc.matrixsdk.services.utils.Mapper;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

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
    public ClientEvent getEvent(RoomID roomId, String eventId) {
        Objects.requireNonNull(eventId, "The event ID must not be null");
        String response = httpTransport.getEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId + "/event/" + eventId), context.token());

        return Mapper.getObjectFromString(response, ClientEvent.class);
    }

    @Override
    public RoomMembers getJoinedMembers(RoomID roomId) {

        String response = httpTransport.getEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId + "/joined_members"), context.token());
        return Mapper.getObjectFromString(response, RoomMembers.class);
    }

    @Override
    public List<ClientEvent> getMembers(RoomID roomId, String at, Membership membership, Membership notMembership) {
        Map<String, Object> args = new HashMap<>();
        args.put("at", at);
        args.put("membership", membership.getValue());
        args.put("not_membership", notMembership.getValue());
        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                ROOM_ENDPOINT + roomId + "/members", args);

        String response = httpTransport.getEvent(uri, context.token());
        // We can skip the chunk parent
        var chunk = Mapper.getArrayFromAJsonKey(response, "chunk");
        List<ClientEvent> clients = new ArrayList<>();
        for (JsonNode client : chunk) {
            clients.add(Mapper.getObjectFromString(client.toString(), ClientEvent.class));
        }
        return clients;

    }

    @Override
    public List<ClientEvent> getStateEvents(RoomID roomId) {
        String response = httpTransport.getEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId + "/state"),
                context.token());

        return Mapper.getObjectFromString(response, new TypeReference<>() {
        });
    }

    @Override
    public List<ClientEvent> getStateEvents(RoomID roomId, String eventType, String stateKey, Format format) {

        Map<String, Object> args = new HashMap<>();
        args.put("format", format.getValue());
        var uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                ROOM_ENDPOINT + roomId + "/state/" + eventType + "/" + stateKey, args);
        String response = httpTransport.getEvent(uri, context.token());

        return Mapper.getObjectFromString(response, new TypeReference<>() {
        });
    }

    @Override
    public Messages getMessages(RoomID roomId, ChronologicalDirection dir, QueryParametersMessages params) {
        // filter is NOT mapped
        Map<String, Object> args = new HashMap<>();
        args.put("dir", dir.getValue());
        args.put("from", params.from());
        args.put("to", params.to());
        args.put("limit", params.limit());
        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(), ROOM_ENDPOINT + roomId + "/messages", args);
        String queryResponse = httpTransport.getEvent(uri, context.token());
        return Mapper.getObjectFromString(queryResponse, Messages.class);
    }

    @Override
    public EventMetadata getEventClosestToTimestamp(RoomID roomId, ChronologicalDirection dir, int unixEpochMiliseconds) {

        if (unixEpochMiliseconds < 0) {
            throw new IllegalArgumentException("Time must be positive");
        }
        Map<String, Object> args = new HashMap<>();
        args.put("dir", dir.getValue());
        args.put("ts", unixEpochMiliseconds);
        var uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(), ROOM_ENDPOINT + roomId, args);
        String response = httpTransport.getEvent(uri, context.token());
        return Mapper.getObjectFromString(response, EventMetadata.class);
    }

    @Override
    public RoomInfo getInitialSync(RoomID roomId) {

        String response = httpTransport.getEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId + "/initialSync"), context.token());
        return Mapper.getObjectFromString(response, RoomInfo.class);
    }

    @Override
    public String sendStateEvent(RoomID roomId, String eventType, String stateKey, RoomStateEvent<?> matrixRoomMessageEvent) {

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(matrixRoomMessageEvent);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse input data", e);
        }

        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(), ROOM_ENDPOINT + roomId + "/state/" + eventType + "/" + stateKey, null);
        String response = httpTransport.putEvent(uri, jsonPayload, context.token());
        return Mapper.getStringValueOfAJsonKey(response, "event_id");
    }

    @Override
    public String sendMessageEvent(RoomID roomId, RoomMessageEvent roomMessageEvent) {

        String jsonPayload;
        try {
            jsonPayload = objectMapper.writeValueAsString(roomMessageEvent);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse input data", e);
        }

        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(), ROOM_ENDPOINT + roomId + "/send/m.room.message/" + UUID.randomUUID(), null);
        String response = httpTransport.putEvent(uri, jsonPayload, context.token());
        return Mapper.getStringValueOfAJsonKey(response, "event_id");

    }

    @Override
    public String redactEvent(RoomID roomId, String eventId, String txnId, String reason) {
        Objects.requireNonNull(eventId, "The event ID" + " must not be null");
        Objects.requireNonNull(txnId, "The transaction ID" + " must not be null");
        String json = null;
        if (reason != null) {
            json = Mapper.createObjectFromMap(Map.ofEntries(Map.entry("reason", reason)));
        }
        String response = httpTransport.putEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId + "/redact/" + eventId + "/" + txnId),
                json, context.token());
        return Mapper.getStringValueOfAJsonKey(response, "event_id");
    }


    /// Creates a new mxc:// for immediate usage.
    ///
    /// @return a [String] representing the MXC
    private String createAndReserveMXC() throws JacksonException {
        String queryResponse = httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + "/_matrix" + "/media/v1/create"), null, this.context.token());

        return Mapper.getStringValueOfAJsonKey(queryResponse, "content_uri");
    }

    @Override
    public String uploadResource(Path resource) {
        try {
            String mxc = createAndReserveMXC();

            String rawPath = mxc.replace("mxc://", "");
            URI uploadTargetUri = URI.create(context.discoveryResponse().homeserver().baseUrl() + "/_matrix/media" + "/v3/upload/" + rawPath + "?filename=" + resource.getFileName().toString());
            httpTransport.putResource(uploadTargetUri, resource, context.token());

            return mxc;

        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public Sync sync(QueryParametersSync params) {
        Map<String, Object> args = new HashMap<>();
        args.put("filter", params.filter());
        args.put("full_state", String.valueOf(params.fullState()));
        args.put("set_presence", params.setPresence());
        args.put("since", params.since());
        args.put("timeout", String.valueOf(params.timeout()));
        args.put("use_state_after", String.valueOf(params.useStateAfter()));
        URI query = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(), "/_matrix/client/v3/sync", args);

        String response = httpTransport.getEvent(query, context.token());
        return Mapper.getObjectFromString(response, Sync.class);

    }

}
