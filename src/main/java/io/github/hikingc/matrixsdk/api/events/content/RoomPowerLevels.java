package io.github.hikingc.matrixsdk.api.events.content;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;

public record RoomPowerLevels(
    Integer ban,
    Map<String, Integer> events,
    Integer eventsDefault,
    Integer invite,
    Integer kick,
    Notifications notifications,
    Integer redact,
    Integer stateDefault,
    Map<String, Integer> users,
    Integer usersDefault)
    implements StateEventContent {

  public record Notifications(
      Integer room,
      Map<String, Object> otherProperties // this type of payload is used in UserProfile too.
      ) {
    /// Deserialization helper to accommodate additional unknown fields.
    ///
    /// @param raw input key-values from a response.
    /// @return deserialized [Notifications] with corresponding values.
    @JsonCreator
    public static Notifications of(Map<String, Object> raw) {
      Map<String, Object> copy = new HashMap<>(raw);
      Integer room = (Integer) copy.remove("room");
      return new Notifications(room, Map.copyOf(copy));
    }
  }
}
