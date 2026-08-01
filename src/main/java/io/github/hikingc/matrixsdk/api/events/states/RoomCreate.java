package io.github.hikingc.matrixsdk.api.events.states;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RoomCreate(List<String> additionalCreators,
                         String creator,
                         @JsonProperty(namespace = "m.federate") Boolean mFederate,
                         PreviousRoom predecessor,
                         String roomVersion,
                         String type) {

    public record PreviousRoom(String eventId,
                               String roomId) {
    }
}
