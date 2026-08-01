package io.github.hikingc.matrixsdk.api.events.states;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RoomTopic(@JsonProperty("m.topic") TopicContentBlock mTopic) {

    public record TopicContentBlock(@JsonProperty("m.text") TextualRepresentation mText) {

        public record TextualRepresentation(String body,
                                            String mimetype) {

        }
    }
}
