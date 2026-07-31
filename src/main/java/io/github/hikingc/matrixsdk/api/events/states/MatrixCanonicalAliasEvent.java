package io.github.hikingc.matrixsdk.api.events.states;

import io.github.hikingc.matrixsdk.api.events.RoomStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;


public record MatrixCanonicalAliasEvent(
        MatrixCanonicalAliasContent content,
        String eventId,
        Long originServerTs,
        String roomId,
        String sender,
        UnsignedData unsigned
) implements RoomStateEvent<MatrixCanonicalAliasContent> {

    @Override
    public String stateKey() {
        return "";
    }

    @Override
    public String type() {
        return "m.room.canonical_alias";
    }
}