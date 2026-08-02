package io.github.hikingc.matrixsdk.api.events.types;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

public record RoomTopicEvent(RoomTopic content,
                             String eventId,
                             Long originServerTs,
                             String roomId,
                             String sender,
                             UnsignedData unsigned
) implements SingletonStateEvent<RoomTopic> {

    @Override
    public String type() {
        return "m.room.topic";
    }

}
