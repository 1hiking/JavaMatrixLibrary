package io.github.hikingc.matrixsdk.api.events.states;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record RoomPowerLevels(Integer ban,
                              Map<String, Integer> events,
                              Integer eventsDefault,
                              Integer invite,
                              Integer kick,
                              Notifications notifications,
                              Integer redact,
                              Integer stateDefault,
                              List<Map<String, String>> users,
                              Integer user_default
) {

    public record Notifications(Integer room,
                                Map<Integer, Object> otherProperties // this type of payload is used in UserProfile too.
    ) {
        /// Deserialization helper to accommodate additional unknown fields.
        ///
        /// @param raw input key-values from a response.
        /// @return deserialized [Notifications] with corresponding values.
        @JsonCreator
        public static Notifications of(Map<Integer, Object> raw) {
            Map<Integer, Object> copy = new HashMap<>(raw);

            Integer room = (Integer) copy.remove("room");


            return new Notifications(room, Map.copyOf(copy));
        }
    }
}


