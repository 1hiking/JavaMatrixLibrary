package org.hik.services.rooms;

import org.hik.api.Room;
import org.hik.api.rooms.*;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.utils.HttpTransport;
import org.hik.services.utils.Mapper;
import org.hik.services.utils.Validator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Main service implementation class of the Room interface, providing all the required endpoints and records to
/// perform activities such as kicking, banning, listing of, and creation of rooms.
public class RoomService implements Room {

    /// Common return field value by many responses.
    public static final String ROOM_ID = "room_id";
    /// Common endpoint for many Room events.
    private static final String ROOM_ENDPOINT = "/_matrix/client/v3/rooms/";
    /// Common endpoint for many Directory events.
    private static final String DIRECTORY_ENDPOINT = "/_matrix/client/v3/directory/list/room/";
    /// Common endpoint for other Directory events.
    private static final String DIRECTORY_ENDPOINT_ROOM = "/_matrix/client/v3/directory/room/";
    private final ObjectMapper objectMapper = Mapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport(10);
    private final ClientContext context;

    public RoomService(ClientContext context) {
        this.context = context;
    }

    @Override
    public String create(boolean isFederated, String name, String aliasName, String topic, CreationRoomType type,
                         boolean isVisible) {

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

        String responseBody;
        try {
            responseBody =
                    httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() +
                                    "/_matrix/client/v3/createRoom"),
                            jsonPayload, context.token());

            return Mapper.getStringFromSingleObject(responseBody, ROOM_ID);

        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void setAlias(String roomAlias, String roomId) {
        Validator.roomAlias(roomAlias);
        Validator.roomId(roomId);
        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                DIRECTORY_ENDPOINT_ROOM + roomAlias, null);

        Map<String, Object> map = new HashMap<>();
        map.put(ROOM_ID, roomId);

        httpTransport.putEvent(uri,
                Mapper.createObjectFromMap(map),
                context.token());
    }

    @Override
    public ResolvedAlias resolveAlias(String roomAlias) {
        Validator.roomAlias(roomAlias);
        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                DIRECTORY_ENDPOINT_ROOM + roomAlias, null);

        var responseBody =
                httpTransport.getEvent(uri,
                        context.token());
        return Mapper.getObjectFromString(responseBody, ResolvedAlias.class);

    }

    @Override
    public void deleteAlias(String roomAlias) {
        Validator.roomAlias(roomAlias);
        URI uri = httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                DIRECTORY_ENDPOINT_ROOM + roomAlias, null);
        httpTransport.deleteEvent(uri, context.token());

    }

    @Override
    public List<String> getAliasesOfARoom(String roomId) {
        Validator.roomId(roomId);

        String response =
                httpTransport.getEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId + "/aliases"),
                        context.token());

        // Can be improved
        var aliases = objectMapper.readTree(response).get("aliases");
        List<String> aliasesList = new ArrayList<>();
        for (JsonNode alias : aliases) {
            aliasesList.add(alias.stringValue());
        }
        return aliasesList;
    }

    @Override
    public JoinedRooms getJoinedRooms() {
        String response =
                httpTransport.getEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                                "/client/v3/joined_rooms"),
                        context.token());

        return Mapper.getObjectFromString(response, JoinedRooms.class);
    }

    @Override
    public void inviteUser(String roomId, RoomMembershipRequest event) {
        Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId + "/invite"),
                    serializedInputData, this.context.token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String joinByRoomIdOrAliasIfAllowed(String roomIdOrAlias, JoinRoomRequest request, List<String> via) {
        Validator.roomIdOrAlias(roomIdOrAlias);
        Map<String, Object> params = new HashMap<>();
        params.put("via", via);
        URI uri = this.httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                "/_matrix/client/v3/join/" + roomIdOrAlias,
                params);
        try {
            var serializedInputData = objectMapper.writeValueAsString(request);
            var responseBody =
                    httpTransport.postEvent(uri,
                            serializedInputData,
                            context.token());
            return Mapper.getStringFromSingleObject(responseBody, ROOM_ID);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String joinByRoomIdIfAllowed(String roomId, JoinRoomRequest request, List<String> via) {
        Map<String, Object> params = new HashMap<>();
        params.put("via", via);
        Validator.roomId(roomId);
        URI uri = this.httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                ROOM_ENDPOINT + roomId + "/join", params);
        try {
            var serializedInputData = objectMapper.writeValueAsString(request);
            var responseBody =
                    httpTransport.postEvent(uri,
                            serializedInputData,
                            context.token());
            return Mapper.getStringFromSingleObject(responseBody, ROOM_ID);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String knockOn(String roomIdOrAlias, String reason, List<String> via) {
        Validator.roomIdOrAlias(roomIdOrAlias);


        URI uri = this.httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                "/_matrix/client/v3/knock/" + roomIdOrAlias, Map.ofEntries(Map.entry("via", via)));
        Map<String, Object> map = new HashMap<>();
        map.put("reason", reason);

        String responseBody = httpTransport.postEvent(uri, Mapper.createObjectFromMap(map),
                context.token());
        try {
            return Mapper.getStringFromSingleObject(responseBody, ROOM_ID);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }


    @Override
    public void forget(String roomId) {
        Validator.roomId(roomId);
        httpTransport.postEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId +
                        "/forget"),
                null, this.context.token());

    }

    @Override
    public void leave(String roomId) {
        Validator.roomId(roomId);
        httpTransport.postEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId +
                        "/leave"),
                null, this.context.token());

    }

    @Override
    public void kick(String roomId, RoomMembershipRequest event) {
        Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(
                    URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId +
                            "/kick"),
                    serializedInputData, this.context.token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void ban(String roomId, RoomMembershipRequest event) {
        Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(
                    URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId +
                            "/ban"),
                    serializedInputData, this.context.token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void unban(String roomId, RoomMembershipRequest event) {
        Validator.roomId(roomId);
        try {
            var responseBody = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(
                    URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + roomId +
                            "/unban"),
                    responseBody, this.context.token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }

    @Override
    public String getRoomDirectoryVisibilityType(String roomId) {
        Validator.roomId(roomId);
        try {
            var responseBody = httpTransport.getEvent(
                    URI.create(context.discoveryResponse().homeserver().baseUrl() + DIRECTORY_ENDPOINT + roomId),
                    null);
            return Mapper.getStringFromSingleObject(responseBody, "visibility");
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void setRoomDirectoryVisibilityType(String roomId, VisibilityRoomType roomType) {
        Validator.roomId(roomId);
        Map<String, Object> map = new HashMap<>();
        map.put("visibility", roomType.getValue());

        httpTransport.putEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + DIRECTORY_ENDPOINT + roomId),
                Mapper.createObjectFromMap(map), this.context.token());
    }


    @Override
    public PublicRoomDirectory getPublishedRoomDirectory(Integer limit, String server, String since) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        if (server != null) params.put("server", server);
        if (since != null) params.put("since", since);

        URI uri = this.httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                "/_matrix/client/v3/publicRooms", params);
        var responseBody = httpTransport.getEvent(uri, context.token());

        return Mapper.getObjectFromString(responseBody, PublicRoomDirectory.class);

    }

    @Override
    public PublicRoomDirectory getPublishedRoomDirectory(PublicRoomRequest request) {
        String serializedInputData = objectMapper.writeValueAsString(request);

        var responseBody = httpTransport.postEvent(URI.create(
                        context.discoveryResponse().homeserver().baseUrl() + "/_matrix/client/v3/publicRooms"),
                serializedInputData, context.token());
        return Mapper.getObjectFromString(responseBody, PublicRoomDirectory.class);

    }

    @Override
    public RoomSummary getRoomSummary(String roomIdOrAlias, List<String> via) {
        Validator.roomIdOrAlias(roomIdOrAlias);
        Map<String, Object> args = new HashMap<>();
        args.put("via", via);

        URI uri = this.httpTransport.generateEncodedURI(context.discoveryResponse().homeserver().baseUrl(),
                "/_matrix/client/v1/room_summary/" + roomIdOrAlias,
                args);
        var responseBody = httpTransport.getEvent(uri, context.token());

        return Mapper.getObjectFromString(responseBody, RoomSummary.class);
    }

}
