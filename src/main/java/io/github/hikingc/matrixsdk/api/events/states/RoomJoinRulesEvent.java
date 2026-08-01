package io.github.hikingc.matrixsdk.api.events.states;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;


public record RoomJoinRulesEvent(
        RoomJoinRules content,
        String eventId,
        Long originServerTs,
        String roomId,
        String sender,
        UnsignedData unsigned
) implements SingletonStateEvent<RoomJoinRules> {


    @Override
    public String type() {
        return "m.room.join_rules";
    }
}