package io.github.hikingc.matrixsdk.api.events.types;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

public record RoomMemberEvent(RoomMember content,
                              String eventId,
                              Long originServerTs,
                              String roomId,
                              String sender,
                              UnsignedData unsigned) implements SingletonStateEvent<RoomMember> {

    @Override
    public String type() {
        return "m.room.member";
    }

}
