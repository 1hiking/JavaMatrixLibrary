package org.hik.services.modules;

import org.hik.api.Room;
import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.payloads.roomstate.*;
import org.hik.services.networking.HttpTransport;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RoomService implements Room {

    /**
     * Common endpoint for many Room events.
     */
    private static final String ROOM_ENDPOINT = "/_matrix/client/v3/rooms/";
    /**
     * Common endpoint for many Directory events.
     */
    private static final String DIRECTORY_ENDPOINT = "/_matrix/client/v3/directory/list/room/";
    private static final String PUBLIC_ROOMS_ENDPOINT = "/_matrix/client/v3/publicRooms";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpTransport httpTransport = new HttpTransport();
    private final ClientContext client;

    public RoomService(ClientContext client) {
        this.client = client;
    }

    @Override
    public String create(boolean isFederated, String name, String aliasName, String topic, CreationRoomType type,
                         boolean isVisible) throws InterruptedException {

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
            queryResponse =
                    httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() +
                                    "/_matrix/client/v3/createRoom"),
                            jsonPayload, client.credentials().token());

            JsonNode responsePayload = objectMapper.readTree(queryResponse);
            JsonNode idNode = responsePayload.path("room_id");
            if (idNode.isMissingNode()) {
                throw new MatrixIOException("Missing 'room_id' in server response ");
            }
            return idNode.stringValue();
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to create a room ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public boolean forget(String roomId) throws InterruptedException {
        var payloadRoomID = Objects.requireNonNull(roomId, "A room id is required.");

        try {
            httpTransport.postEvent(
                    URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomID +
                            "/forget"),
                    null, this.client.credentials().token());
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to forget the room ", e);
        }
        return true;

    }

    @Override
    public boolean leave(String roomId) throws InterruptedException {
        var payloadRoomId = Objects.requireNonNull(roomId, "A room id is required.");
        try {
            httpTransport.postEvent(
                    URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId +
                            "/leave"),
                    null, this.client.credentials().token());
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to leave the room ", e);
        }
        return true;

    }

    @Override
    public boolean kick(String roomId, RoomMembershipRequest event) throws InterruptedException {
        var payloadRoomId = Objects.requireNonNull(roomId, "A room id is required.");
        try {
            var jsonPayload = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/kick"),
                    jsonPayload, this.client.credentials().token());
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to kick someone from a room ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
        return true;
    }

    @Override
    public boolean ban(String roomId, RoomMembershipRequest event) throws InterruptedException {
        var payloadRoomId = Objects.requireNonNull(roomId, "A room id is required.");
        try {
            var jsonPayload = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/ban"),
                    jsonPayload, this.client.credentials().token());
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to kick someone from a room ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
        return true;
    }

    @Override
    public boolean unban(String roomId, RoomMembershipRequest event) throws InterruptedException {
        var payloadRoomId = Objects.requireNonNull(roomId);
        try {
            var jsonPayload = objectMapper.writeValueAsString(event);
            httpTransport.postEvent(URI.create(client.discoveryResponse().homeserver().baseUrl() + ROOM_ENDPOINT + payloadRoomId + "/unban"),
                    jsonPayload, this.client.credentials().token());
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to kick someone from a room ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
        return true;
    }

    @Override
    public String getRoomDirectoryVisibilityType(String roomId) throws InterruptedException {
        var payloadRoomId = Objects.requireNonNull(roomId, "A room id is required.");
        try {
            var queryResponse = httpTransport.getEvent(
                    URI.create(client.discoveryResponse().homeserver().baseUrl() + DIRECTORY_ENDPOINT + payloadRoomId),
                    null);
            return objectMapper.readTree(queryResponse).get("visibility").stringValue();
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to kick someone from a room ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public boolean setRoomDirectoryVisibilityType(String roomId, VisibilityRoomType roomType) throws InterruptedException {
        var payloadRoomId = Objects.requireNonNull(roomId, "A room id is required.");

        // It is faster in a case like this where only 1 parameter is expected.
        String json =
                """
                {"visibility": "%s"}
                """.formatted(roomType.getValue());

        try {
            httpTransport.putEvent(
                    URI.create(client.discoveryResponse().homeserver().baseUrl() + DIRECTORY_ENDPOINT + payloadRoomId),
                    json, this.client.credentials().token());
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to kick someone from a room ", e);
        }
        return true;
    }


    // 26/june/2026: There is a POST equivalent for this endpoint, however I don't see the benefit of implementing it
    // as of now.

    @Override
    public PublicRoomDirectory getPublishedRoomDirectory(Integer limit, String server, String since) throws InterruptedException {
        Map<String, Object> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        if (server != null) params.put("server", server);
        if (since != null) params.put("since", since);


        String finalUrl =
                this.buildUrlArgs(client.discoveryResponse().homeserver().baseUrl() + PUBLIC_ROOMS_ENDPOINT, params);
        try {
            var queryResponse = httpTransport.getEvent(URI.create(finalUrl), client.credentials().token());
            return objectMapper.readValue(queryResponse, PublicRoomDirectory.class);
        } catch (IOException e) {
            throw new MatrixIOException("Network error while attempting to fetch messages ", e);
        } catch (JacksonException e) {
            throw new MatrixIOException("Failed to parse Matrix response JSON ", e);
        }
    }

    @Override
    public RoomSummary getRoomSummary(String roomIdOrAlias, URI via) throws InterruptedException {
        String idToUse = Objects.requireNonNull(roomIdOrAlias, "A room id or room alias is required.");

        String finalUrl = this.buildUrlArgs(client.discoveryResponse().homeserver().baseUrl() + "/_matrix/client/v1" +
                        "/room_summary/" + idToUse,
                Map.ofEntries(Map.entry("via", via)));

        try {
            var queryResponse = httpTransport.getEvent(URI.create(finalUrl), client.credentials().token());
            return objectMapper.readValue(queryResponse, RoomSummary.class);
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
