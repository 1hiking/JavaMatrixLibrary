package io.github.hikingc.matrixsdk.api.events.states;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

public record RoomMemberEvent<RoomMemberContent>(RoomMemberContent content,
                                                 String eventId,
                                                 Long originServerTs,
                                                 String roomId,
                                                 String sender,
                                                 UnsignedData unsigned) implements SingletonStateEvent<RoomMemberContent> {

    @Override
    public String type() {
        return "m.room.member";
    }

}
