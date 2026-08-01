package io.github.hikingc.matrixsdk.api.events.states;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

public record RoomPinnedEventEvents(RoomPinnedEvents content,
                                    String eventId,
                                    Long originServerTs,
                                    String roomId,
                                    String sender,
                                    UnsignedData unsigned) implements SingletonStateEvent<RoomPinnedEvents> {


    @Override
    public String type() {
        return "m.room.pinned_events";
    }
}
