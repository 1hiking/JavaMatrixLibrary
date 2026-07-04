package org.hik.services.rooms;

import org.hik.api.Room;
import org.hik.api.rooms.*;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.services.utils.ConfiguratedMapper;
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

public class RoomService implements Room {

    /// Common endpoint for many Room events.
    private static final String ROOM_ENDPOINT = "/_matrix/client/v3/rooms/";
    /// Common endpoint for many Directory events.
    private static final String DIRECTORY_ENDPOINT = "/_matrix/client/v3/directory/list/room/";
    /// Common endpoint for other Directory events.
    private static final String DIRECTORY_ENDPOINT_ROOM = "/_matrix/client/v3/directory/room/";
    private final ObjectMapper objectMapper = ConfiguratedMapper.getInstance();
    private final HttpTransport httpTransport = new HttpTransport();
    private final ClientContext client;

    public RoomService(ClientContext client) {
        this.client = client;
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
                    httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() +
                                    "/_matrix/client/v3/createRoom"),
                            jsonPayload, client.credentials().token());

            JsonNode responsePayload = objectMapper.readTree(responseBody);
            JsonNode idNode = responsePayload.path("room_id");
            if (idNode.isMissingNode()) {
                throw new MatrixIOException("Missing 'room_id' in server response ");
            }
            return idNode.stringValue();
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
            URI base = URI.create(client.discoveryResponse().homeserver().baseUrl());
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
                client.credentials().token());
    }

    @Override
    public ResolvedAlias resolveAlias(String roomAlias) {
        var payloadRoom = Validator.roomAlias(roomAlias);
        URI uri = null;
        try {
            URI base = URI.create(client.discoveryResponse().homeserver().baseUrl());
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
                        client.credentials().token());
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
            URI base = URI.create(client.discoveryResponse().homeserver().baseUrl());
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
        httpTransport.deleteEvent(uri, client.credentials().token());

    }

    @Override
    public RoomAliases getAliasesOfARoom(String roomAlias) {
        var payloadRoomId = Validator.roomAlias(roomAlias);

        String response =
                httpTransport.getEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/aliases"),
                        client.credentials().token());
        try {
            return objectMapper.readValue(response, RoomAliases.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public JoinedRooms getJoinedRooms() {
        String response =
                httpTransport.getEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                                "/client/v3/joined_rooms"),
                        client.credentials().token());
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
            httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/invite"),
                    serializedInputData, this.client.credentials().token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String joinByRoomIdOrAliasIfAllowed(String roomIdOrAlias, JoinRoomRequest request) {
        var payloadRoomId = Validator.roomIdOrAlias(roomIdOrAlias);
        try {
            var serializedInputData = objectMapper.writeValueAsString(request);
            var responseBody =
                    httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                                    "/client" +
                                    "/v3/join/" + payloadRoomId),
                            serializedInputData,
                            client.credentials().token());
            JsonNode responsePayload = objectMapper.readTree(responseBody);
            return responsePayload.path("room_id").stringValue();
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String joinByRoomIdIfAllowed(String roomId, JoinRoomRequest request) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(request);
            var responseBody =
                    httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/join"),
                            serializedInputData,
                            client.credentials().token());
            JsonNode responsePayload = objectMapper.readTree(responseBody);
            return responsePayload.path("room_id").stringValue();
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public String knockOn(String roomIdOrAlias, String reason, List<String> via) {
        var payloadRoomID = Validator.roomIdOrAlias(roomIdOrAlias);
        var params = new HashMap<String, Object>();
        params.put("via", via);

        var url = this.httpTransport.buildUrlArgs(
                this.client.discoveryResponse().homeserver().baseUrl()
                        + "/_matrix/client/v3/knock/"
                        + payloadRoomID,
                params);
        var reasonBody = """
                {"reason" : "%s"}
                """.formatted(reason);
        String responseBody = httpTransport.postEvent(URI.create(url), reasonBody, client.credentials().token());
        try {
            return objectMapper.readTree(responseBody).get("room_id").stringValue();
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }


    @Override
    public void forget(String roomId) {
        var payloadRoomID = Validator.roomId(roomId);
        httpTransport.postEvent(
                URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomID +
                        "/forget"),
                null, this.client.credentials().token());

    }

    @Override
    public void leave(String roomId) {
        var payloadRoomId = Validator.roomId(roomId);
        httpTransport.postEvent(
                URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                        "/leave"),
                null, this.client.credentials().token());

    }

    @Override
    public void kick(String roomId, RoomMembershipRequest event) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/kick"),
                    serializedInputData, this.client.credentials().token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void ban(String roomId, RoomMembershipRequest event) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var serializedInputData = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/ban"),
                    serializedInputData, this.client.credentials().token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void unban(String roomId, RoomMembershipRequest event) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var responseBody = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/unban"),
                    responseBody, this.client.credentials().token());
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }

    @Override
    public String getRoomDirectoryVisibilityType(String roomId) {
        var payloadRoomId = Validator.roomId(roomId);
        try {
            var responseBody = httpTransport.getEvent(
                    URI.create(client.discoveryResponse().homeserver().baseUrl() + DIRECTORY_ENDPOINT + payloadRoomId),
                    null);
            return objectMapper.readTree(responseBody).get("visibility").stringValue();
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public void setRoomDirectoryVisibilityType(String roomId, VisibilityRoomType roomType) {
        var payloadRoomId = Validator.roomId(roomId);

        // It is faster in a case like this where only 1 parameter is expected.
        String rawStringPayload =
                """
                        {"visibility": "%s"}
                        """.formatted(roomType.getValue());

        httpTransport.putEvent(
                URI.create(client.discoveryResponse().homeserver().baseUrl() + DIRECTORY_ENDPOINT + payloadRoomId),
                rawStringPayload, this.client.credentials().token());
    }


    @Override
    public PublicRoomDirectory getPublishedRoomDirectory(Integer limit, String server, String since) {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        if (server != null) params.put("server", server);
        if (since != null) params.put("since", since);


        String url = this.httpTransport.buildUrlArgs(client.discoveryResponse().homeserver().baseUrl() + "/_matrix/client" +
                                "/v3/publicRooms", params);
        try {
            var responseBody = httpTransport.getEvent(URI.create(url), client.credentials().token());
            return objectMapper.readValue(responseBody, PublicRoomDirectory.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public PublicRoomDirectory getPublishedRoomDirectory(PublicRoomRequest request) {

        try {
            String serializedInputData = objectMapper.writeValueAsString(request);

            var responseBody = httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() +
                    "/_matrix/client/v3" +
                    "/publicRooms"
            ), serializedInputData, client.credentials().token());
            return objectMapper.readValue(responseBody, PublicRoomDirectory.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public RoomSummary getRoomSummary(String roomIdOrAlias, List<String> via) {
        String idToUse = Validator.roomIdOrAlias(roomIdOrAlias);

        String url = this.httpTransport.buildUrlArgs(client.discoveryResponse().homeserver().baseUrl() + "/_matrix" +
                        "/client/v1" +
                        "/room_summary/" + idToUse,
                Map.ofEntries(Map.entry("via", via)));

        try {
            var responseBody = httpTransport.getEvent(URI.create(url), client.credentials().token());
            return objectMapper.readValue(responseBody, RoomSummary.class);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }

    }

}
