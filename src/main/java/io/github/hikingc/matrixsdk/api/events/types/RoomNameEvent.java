package io.github.hikingc.matrixsdk.api.events.types;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

public record RoomNameEvent(RoomName content,
                            String eventId,
                            Long originServerTs,
                            String roomId,
                            String sender,
                            UnsignedData unsigned) implements SingletonStateEvent<RoomName> {

    @Override
    public String type() {
        return "m.room.name";
    }
}
