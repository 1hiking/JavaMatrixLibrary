package org.hik.services.modules;

import org.hik.context.ClientContext;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
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

public class Room {

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

    public Room(ClientContext client) {
        this.client = client;
    }

    /// Creates a room, this method will let the homeserver choose the default configuration for most tasks
    /// and the following parameters will overwrite them if set to a non-null value.
    ///
    /// @param isFederated if the room will be federated
    /// @param name        the room's name, if any.
    /// @param aliasName   the room's canonical alias, if any
    /// @param topic       the room's topic, if any.
    /// @param type        the [CreationRoomType]
    /// @param isVisible   if the room will be visible to the public
    /// @return the created room’s ID.
    /// @throws MatrixIOException      when the payload cannot be processed
    /// @throws MatrixNetworkException when the response status is not successful
    /// @throws InterruptedException   when the HTTP Client is interrupted
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

    /// Sends a request to leave the room, upon success, you will forget all messages from this room.
    /// If all users on a room forget it, the room is eligible for deletion.
    /// You must [forget](#forget(String)) the room first before calling this method.
    ///
    /// @param roomId the target room ID.
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException      when the payload cannot be processed.
    /// @throws MatrixNetworkException when the response status is not successful.
    /// @throws InterruptedException   when the HTTP Client is interrupted.
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

    /// Sends a request to leave the room, upon success, you will no longer receive new messages from this room.
    /// If the user was invited to the room, but had not joined, this call serves to reject the invite.
    /// Some servers MAY additionally `forget` the room when leaving.
    ///
    /// @param roomId the target room ID.
    /// @return true if the roomId was set and the request finished with no issue.
    /// @throws MatrixIOException      when the payload cannot be processed
    /// @throws MatrixNetworkException when the response status is not successful
    /// @throws InterruptedException   when the HTTP Client is interrupted
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

    /// Sends a request to kick someone from a room. Caller must have a configured power level to perform this
    /// operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
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

    /// Sends a request to ban someone from a room. Caller must have a configured power level to perform this operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
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

    /// Sends a request to unban someone from a room. Caller must have a configured power level to perform this
    /// operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
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

    /// Gets the visibility of a given room in the server’s published room directory.
    /// Authentication is not required to run this request.
    /// NOTE: This does NOT guarantee join rules are public.
    ///
    /// @param roomId the target room ID.
    /// @return a [String] with the room visibility.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
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

    /// Sets the visibility of a given room in the server’s published room directory.
    ///
    /// @param roomId   the target room ID.
    /// @param roomType a [VisibilityRoomType] with the room visibility type
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
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

    /// Lists a server’s published room directory.
    ///
    /// @param limit  limit of records to show
    /// @param server what server to fetch from, if not supplied it will fetch the local server. Case-sensitive.
    /// @param since  a pagination token from a previous request, allowing you to get the next or previous
    /// batch of rooms. The direction of pagination is specified by which token is supplied (not like
    ///  [ChronologicalDirectionType]).
    /// @return a [PublicRoomDirectory] containing [PublishedRoomsChunk] records of the published rooms on the server.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
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

    /// Retrieves a summary for a room. The response data might yield outdated, partial or even no data.
    ///
    /// @param roomIdOrAlias the room id or alias of the room to target
    /// @param via           the servers to attempt to request the summary from when the local server cannot generate it
    /// @return a [RoomSummary] containing all the information about the room.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
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
