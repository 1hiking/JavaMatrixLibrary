package io.github.hikingc.matrixsdk.services.events;

import io.github.hikingc.matrixsdk.api.Event;
import io.github.hikingc.matrixsdk.api.events.*;
import io.github.hikingc.matrixsdk.api.events.content.*;
import io.github.hikingc.matrixsdk.api.events.model.RoomMemberEvent;
import io.github.hikingc.matrixsdk.api.events.queries.*;
import io.github.hikingc.matrixsdk.api.events.sync.Sync;
import io.github.hikingc.matrixsdk.api.identifiers.RoomID;
import io.github.hikingc.matrixsdk.context.ClientContext;
import io.github.hikingc.matrixsdk.exceptions.MatrixIOException;
import io.github.hikingc.matrixsdk.services.utils.HttpTransport;
import io.github.hikingc.matrixsdk.services.utils.Mapper;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@NullMarked
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
  public ClientEvent<?> getEvent(RoomID roomId, String eventId) {
    Objects.requireNonNull(eventId, "The event ID must not be null");
    String response =
        httpTransport.getEvent(
            URI.create(
                context.discoveryResponse().homeserver().baseUrl()
                    + ROOM_ENDPOINT
                    + roomId
                    + "/event/"
                    + eventId),
            context.token());

    return Mapper.getObjectFromString(response, ClientEvent.class);
  }

  @Override
  public RoomMembers getJoinedMembers(RoomID roomId) {

    String response =
        httpTransport.getEvent(
            URI.create(
                context.discoveryResponse().homeserver().baseUrl()
                    + ROOM_ENDPOINT
                    + roomId
                    + "/joined_members"),
            context.token());
    return Mapper.getObjectFromString(response, RoomMembers.class);
  }

  @Override
  public List<RoomMemberEvent> getMembers(
      RoomID roomId, String at, Membership membership, Membership notMembership) {
    Map<String, Object> args = new HashMap<>();
    args.put("at", at);
    args.put("membership", membership.getValue());
    args.put("not_membership", notMembership.getValue());
    URI uri =
        httpTransport.generateEncodedURI(
            context.discoveryResponse().homeserver().baseUrl(),
            ROOM_ENDPOINT + roomId + "/members",
            args);

    String response = httpTransport.getEvent(uri, context.token());
    // We can skip the chunk parent, we don't use ObjectFromString because it is NOT a raw Array as
    // detailed
    // on the spec.
    return Mapper.getListFromAJsonKey(response, "chunk", RoomMemberEvent.class);
  }

  @Override
  public List<ClientEvent<?>> getStateEvents(RoomID roomId) {
    String response =
        httpTransport.getEvent(
            URI.create(
                context.discoveryResponse().homeserver().baseUrl()
                    + ROOM_ENDPOINT
                    + roomId
                    + "/state"),
            context.token());

    return Mapper.getObjectFromString(response, new TypeReference<>() {});
  }

  @Override
  public ClientEvent<?> getStateEvent(RoomID roomId, String eventType, String stateKey) {

    Map<String, Object> args = new HashMap<>();
    args.put("format", Format.EVENT.getValue()); // Hardcode this for now
    var uri =
        httpTransport.generateEncodedURI(
            context.discoveryResponse().homeserver().baseUrl(),
            ROOM_ENDPOINT + roomId + "/state/" + eventType + "/" + stateKey,
            args);
    String response = httpTransport.getEvent(uri, context.token());

    return Mapper.getObjectFromString(response, ClientEvent.class);
  }

  @Override
  public Messages getMessages(
      RoomID roomId, ChronologicalDirection dir, QueryParametersMessages params) {
    // filter is NOT mapped
    Map<String, Object> args = new HashMap<>();
    args.put("dir", dir.getValue());
    args.put("from", params.from());
    args.put("to", params.to());
    args.put("limit", params.limit());
    URI uri =
        httpTransport.generateEncodedURI(
            context.discoveryResponse().homeserver().baseUrl(),
            ROOM_ENDPOINT + roomId + "/messages",
            args);
    String queryResponse = httpTransport.getEvent(uri, context.token());
    return Mapper.getObjectFromString(queryResponse, Messages.class);
  }

  @Override
  public EventMetadata getEventClosestToTimestamp(
      RoomID roomId, ChronologicalDirection dir, int unixEpochMiliseconds) {

    if (unixEpochMiliseconds < 0) {
      throw new IllegalArgumentException("Time must be positive");
    }
    Map<String, Object> args = new HashMap<>();
    args.put("dir", dir.getValue());
    args.put("ts", unixEpochMiliseconds);
    var uri =
        httpTransport.generateEncodedURI(
            context.discoveryResponse().homeserver().baseUrl(), ROOM_ENDPOINT + roomId, args);
    String response = httpTransport.getEvent(uri, context.token());
    return Mapper.getObjectFromString(response, EventMetadata.class);
  }

  @Override
  public RoomInfo getInitialSync(RoomID roomId) {

    String response =
        httpTransport.getEvent(
            URI.create(
                context.discoveryResponse().homeserver().baseUrl()
                    + ROOM_ENDPOINT
                    + roomId
                    + "/initialSync"),
            context.token());
    return Mapper.getObjectFromString(response, RoomInfo.class);
  }

  @Override
  public String sendStateEvent(RoomID roomId, String stateKey, StateEventContent content) {
    String jsonPayload;
    try {
      jsonPayload = objectMapper.writeValueAsString(content);
    } catch (JacksonException e) {
      throw new MatrixIOException("Failed to parse input data", e);
    }
    String type = resolveStateWireType(content);

    URI uri =
        httpTransport.generateEncodedURI(
            context.discoveryResponse().homeserver().baseUrl(),
            ROOM_ENDPOINT + roomId + "/state/" + type + "/" + stateKey,
            null);
    String response = httpTransport.putEvent(uri, jsonPayload, context.token());
    return Mapper.getStringValueOfAJsonKey(response, "event_id");
  }

  @Override
  public String sendMessageEvent(RoomID roomId, String txnId, MessageEventContent content) {
    Objects.requireNonNull(txnId, "The transaction id is required.");
    String type = resolveMessageWireType(content);
    String jsonPayload;
    try {
      jsonPayload = objectMapper.writeValueAsString(content);
    } catch (JacksonException e) {
      throw new MatrixIOException("Failed to parse input data", e);
    }

    URI uri =
        httpTransport.generateEncodedURI(
            context.discoveryResponse().homeserver().baseUrl(),
            ROOM_ENDPOINT + roomId + "/send/" + type + "/" + txnId,
            null);
    String response = httpTransport.putEvent(uri, jsonPayload, context.token());
    return Mapper.getStringValueOfAJsonKey(response, "event_id");
  }

  @Override
  public String redactEvent(RoomID roomId, String eventId, String txnId, @Nullable String reason) {
    Objects.requireNonNull(eventId, "The event ID" + " must not be null");
    Objects.requireNonNull(txnId, "The transaction ID" + " must not be null");
    String json = null;
    if (reason != null) {
      json = Mapper.createObjectFromMap(Map.ofEntries(Map.entry("reason", reason)));
    }
    String response =
        httpTransport.putEvent(
            URI.create(
                context.discoveryResponse().homeserver().baseUrl()
                    + ROOM_ENDPOINT
                    + roomId
                    + "/redact/"
                    + eventId
                    + "/"
                    + txnId),
            json,
            context.token());
    return Mapper.getStringValueOfAJsonKey(response, "event_id");
  }

  @Override
  public String uploadResource(Path resource) {
    try {
      String mxc = createAndReserveMXC();

      String rawPath = mxc.replace("mxc://", "");
      URI uploadTargetUri =
          URI.create(
              context.discoveryResponse().homeserver().baseUrl()
                  + "/_matrix/media"
                  + "/v3/upload/"
                  + rawPath
                  + "?filename="
                  + resource.getFileName().toString());
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
    URI query =
        httpTransport.generateEncodedURI(
            context.discoveryResponse().homeserver().baseUrl(), "/_matrix/client/v3/sync", args);

    String response = httpTransport.getEvent(query, context.token());
    return Mapper.getObjectFromString(response, Sync.class);
  }

  /// Creates a new mxc:// for immediate usage.
  ///
  /// @return a [String] representing the MXC
  private String createAndReserveMXC() throws JacksonException {
    String queryResponse =
        httpTransport.postEvent(
            URI.create(
                context.discoveryResponse().homeserver().baseUrl()
                    + "/_matrix"
                    + "/media/v1/create"),
            null,
            this.context.token());

    return Mapper.getStringValueOfAJsonKey(queryResponse, "content_uri");
  }

  /// Method in charge of returning the correct type of each state content record
  ///
  /// @param content a [StateEventContent] record.
  /// @return it's type as defined in the spec.
  private static String resolveStateWireType(StateEventContent content) {
    return switch (content) {
      case RoomCreate _ -> "m.room.create";
      case RoomGuestAccess _ -> "m.room.guest_access";
      case RoomHistoryVisibility _ -> "m.room.history_visibility";
      case RoomJoinRules _ -> "m.room.join_rules";
      case RoomName _ -> "m.room.name";
      case RoomPinnedEvents _ -> "m.room.pinned_events";
      case RoomPowerLevels _ -> "m.room.power_levels";
      case RoomTopic _ -> "m.room.topic";
      case RoomAvatar _ -> "m.room.avatar";
      case RoomCanonicalAlias _ -> "m.room.canonical_alias";
      case RoomMember _ -> "m.room.member";
    };
  }

  /// Method in charge of returning the correct type of each message content record
  ///
  /// @param content a [MessageEventContent] record.
  /// @return it's type as defined in the spec.
  private static String resolveMessageWireType(MessageEventContent content) {
    return switch (content) {
      case RoomMessage _ -> "m.room.message";
    };
  }
}
