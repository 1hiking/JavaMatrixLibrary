package io.github.hikingc.matrixsdk.api.events.types;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

public record RoomPinnedEventsEvent(RoomPinnedEvents content, // Yeah, I know...
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
