package io.github.hikingc.matrixsdk.api.events.states;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

public record RoomHistoryVisibilityEvent(RoomHistoryVisibility content,
                                         String eventId,
                                         Long originServerTs,
                                         String roomId,
                                         String sender,
                                         UnsignedData unsigned) implements SingletonStateEvent<RoomHistoryVisibility> {
    @Override
    public String type() {
        return "m.room.history_visibility";
    }
}
