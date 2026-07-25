package io.github.hikingc.matrixsdk.api.events.states;

import io.github.hikingc.matrixsdk.api.events.RoomStateEvent;

import java.util.List;
import java.util.Map;


public record MatrixCanonicalAliasEvent(
        MatrixCanonicalAliasContent content,
        String eventId,
        Long originServerTs,
        String roomId,
        String sender,
        List<Map<String, Object>> unsigned
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