package io.github.hikingc.matrixsdk.api.events.types;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;


public record RoomCanonicalAliasEvent(
        RoomCanonicalAlias content,
        String eventId,
        Long originServerTs,
        String roomId,
        String sender,
        UnsignedData unsigned
) implements SingletonStateEvent<RoomCanonicalAlias> {


    @Override
    public String type() {
        return "m.room.canonical_alias";
    }
}