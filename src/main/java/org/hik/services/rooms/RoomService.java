package org.hik.services.rooms;

import org.hik.api.Room;
import org.hik.api.rooms.*;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.utils.ConfigurationMapper;
import org.hik.services.utils.HttpTransport;
import org.hik.services.utils.Validator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// Main service implementation class of the Room interface, providing all the required endpoints and records to
/// perform activities such as kicking, banning, listing of, and creation of rooms.
public class RoomService implements Room {

    /// Common endpoint for many Room events.
    private static final String ROOM_ENDPOINT = "/_matrix/client/v3/rooms/";
    /// Common endpoint for many Directory events.
    private static final String DIRECTORY_ENDPOINT = "/_matrix/client/v3/directory/list/room/";
    /// Common endpoint for other Directory events.
    private static final String DIRECTORY_ENDPOINT_ROOM = "/_matrix/client/v3/directory/room/";
    private final ObjectMapper objectMapper = ConfigurationMapper.getInstance();
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

        String responseBody = null;
        try {
            responseBody =
                    httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() +
                                    "/_matrix/client/v3/createRoom"),
                            jsonPayload, context.credentials().token());

            return ConfigurationMapper.getStringFromSingleObject(responseBody, "room_id");

        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void setAlias(String roomAlias, String roomId) {
        var payloadRoomAlias = Validator.roomAlias(roomAlias);
        var payloadRoomId = Validator.roomId(roomId);
        URI uri = null;
        try {
            URI base = URI.create(context.discoveryResponse().homeserver().baseUrl());
            uri = new URI(
                    base.getScheme(),
                    base.getAuthority(),
                    DIRECTORY_ENDPOINT_ROOM + payloadRoomAlias,
                    null,
                    null
            );
        } catch (URISyntaxException e) {
            throw new MatrixIOException("Failure parsing URI", e);
        }
        // It is faster in a case like this where only 1 parameter is expected.
        var rawStringPayloadRoomId =
                """
                        {"room_id": "%s"}
                        """.formatted(payloadRoomId);

        httpTransport.putEvent(uri,
                rawStringPayloadRoomId,
                context.credentials().token());
    }

    @Override
    public ResolvedAlias resolveAlias(String roomAlias) {
        var payloadRoom = Validator.roomAlias(roomAlias);
        URI uri = null;
        try {
            URI base = URI.create(context.discoveryResponse().homeserver().baseUrl());
            uri = new URI(
                    base.getScheme(),
                    base.getAuthority(),
                    DIRECTORY_ENDPOINT_ROOM + payloadRoom,
                    null,
                    null
            );
        } catch (URISyntaxException e) {
            throw new MatrixIOException("Failure parsing URI", e);
        }
        var responseBody =
                httpTransport.getEvent(uri,
                        context.credentials().token());
        try {
            return objectMapper.readValue(responseBody, ResolvedAlias.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);

        }
    }

    @Override
    public void deleteAlias(String roomAlias) {
        var payloadRoomId = Validator.roomAlias(roomAlias);
        URI uri = null;
        try {
            URI base = URI.create(context.discoveryResponse().homeserver().baseUrl());
            uri = new URI(
                    base.getScheme(),
                    base.getAuthority(),
                    DIRECTORY_ENDPOINT_ROOM + payloadRoomId,
                    null,
                    null
            );
        } catch (URISyntaxException e) {
            throw new MatrixIOException("Failure parsing URI", e);
        }
        httpTransport.deleteEvent(uri, context.credentials().token());

    }

    @Override
    public RoomAliases getAliasesOfARoom(String roomAlias) {
        var payloadRoomId = Validator.roomAlias(roomAlias);

        String response =
                httpTransport.getEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/aliases"),
                        context.credentials().token());
        try {
            return objectMapper.readValue(response, RoomAliases.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public JoinedRooms getJoinedRooms() {
        String response =
                httpTransport.getEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                                "/client/v3/joined_rooms"),
                        context.credentials().token());
        try {
            return objectMapper.readValue(response, JoinedRooms.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void inviteUser(String roomId, RoomMembershipRequest event) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/invite"),
                    serializedInputData, this.context.credentials().token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String joinByRoomIdOrAliasIfAllowed(String roomIdOrAlias, JoinRoomRequest request, List<String> via) {
        var payloadRoomId = Validator.roomIdOrAlias(roomIdOrAlias);
        Map<String, Object> params = new HashMap<>();
        params.put("via", via);
        var url = this.httpTransport.buildUrlArgs(
                context.discoveryResponse().homeserver().baseUrl() + "/_matrix/client/v3/join/" + payloadRoomId,
                params);
        try {
            var serializedInputData = objectMapper.writeValueAsString(request);
            var responseBody =
                    httpTransport.postEvent(URI.create(url),
                            serializedInputData,
                            context.credentials().token());
            return ConfigurationMapper.getStringFromSingleObject(responseBody, "room_id");
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String joinByRoomIdIfAllowed(String roomId, JoinRoomRequest request, List<String> via) {
        Map<String, Object> params = new HashMap<>();
        params.put("via", via);
        var payloadRoomId = Validator.roomId(roomId);
        var url = this.httpTransport.buildUrlArgs(
                context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/join",
                params
        );
        try {
            var serializedInputData = objectMapper.writeValueAsString(request);
            var responseBody =
                    httpTransport.postEvent(URI.create(url),
                            serializedInputData,
                            context.credentials().token());
            JsonNode responsePayload = objectMapper.readTree(responseBody);
            return responsePayload.path("room_id").stringValue();
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String knockOn(String roomIdOrAlias, String reason, List<String> via) {
        var payloadRoomID = Validator.roomIdOrAlias(roomIdOrAlias);


        String url = this.httpTransport.buildUrlArgs(
                this.context.discoveryResponse().homeserver().baseUrl()
                        + "/_matrix/client/v3/knock/"
                        + payloadRoomID,
                Map.ofEntries(Map.entry("via", via)));

        String reasonBody = """
                {"reason" : "%s"}
                """.formatted(reason);
        String responseBody = httpTransport.postEvent(URI.create(url), reasonBody, context.credentials().token());
        try {
            return ConfigurationMapper.getStringFromSingleObject(responseBody, "room_id");
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }


    @Override
    public void forget(String roomId) {
        var payloadRoomID = Validator.roomId(roomId);
        httpTransport.postEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomID +
                        "/forget"),
                null, this.context.credentials().token());

    }

    @Override
    public void leave(String roomId) {
        var payloadRoomId = Validator.roomId(roomId);
        httpTransport.postEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                        "/leave"),
                null, this.context.credentials().token());

    }

    @Override
    public void kick(String roomId, RoomMembershipRequest event) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(
                    URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                            "/kick"),
                    serializedInputData, this.context.credentials().token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void ban(String roomId, RoomMembershipRequest event) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(
                    URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                            "/ban"),
                    serializedInputData, this.context.credentials().token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void unban(String roomId, RoomMembershipRequest event) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var responseBody = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(
                    URI.create(context.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                            "/unban"),
                    responseBody, this.context.credentials().token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }

    @Override
    public String getRoomDirectoryVisibilityType(String roomId) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var responseBody = httpTransport.getEvent(
                    URI.create(context.discoveryResponse().homeserver().baseUrl() + DIRECTORY_ENDPOINT + payloadRoomId),
                    null);
            return ConfigurationMapper.getStringFromSingleObject(responseBody, "visibility");
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void setRoomDirectoryVisibilityType(String roomId, VisibilityRoomType roomType) {
        var payloadRoomId = Validator.roomId(roomId);
        String rawStringPayload =
                """
                        {"visibility": "%s"}
                        """.formatted(roomType.getValue());

        httpTransport.putEvent(
                URI.create(context.discoveryResponse().homeserver().baseUrl() + DIRECTORY_ENDPOINT + payloadRoomId),
                rawStringPayload, this.context.credentials().token());
    }


    @Override
    public PublicRoomDirectory getPublishedRoomDirectory(Integer limit, String server, String since) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        if (server != null) params.put("server", server);
        if (since != null) params.put("since", since);


        String url = this.httpTransport.buildUrlArgs(
                context.discoveryResponse().homeserver().baseUrl() + "/_matrix/client/v3/publicRooms", params);
        try {
            var responseBody = httpTransport.getEvent(URI.create(url), context.credentials().token());
            return objectMapper.readValue(responseBody, PublicRoomDirectory.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public PublicRoomDirectory getPublishedRoomDirectory(PublicRoomRequest request) {
        try {
            String serializedInputData = objectMapper.writeValueAsString(request);

            var responseBody = httpTransport.postEvent(URI.create(
                            context.discoveryResponse().homeserver().baseUrl() + "/_matrix/client/v3/publicRooms"),
                    serializedInputData, context.credentials().token());
            return objectMapper.readValue(responseBody, PublicRoomDirectory.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public RoomSummary getRoomSummary(String roomIdOrAlias, List<String> via) {
        String idToUse = Validator.roomIdOrAlias(roomIdOrAlias);

        String url = this.httpTransport.buildUrlArgs(
                context.discoveryResponse().homeserver().baseUrl() + "/_matrix/client/v1/room_summary/" + idToUse,
                Map.ofEntries(Map.entry("via", via)));

        try {
            var responseBody = httpTransport.getEvent(URI.create(url), context.credentials().token());
            return objectMapper.readValue(responseBody, RoomSummary.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }

}
