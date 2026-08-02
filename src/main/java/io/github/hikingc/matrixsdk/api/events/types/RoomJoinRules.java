package io.github.hikingc.matrixsdk.api.events.types;

public record RoomJoinRules(AllowCondition allow,
                           String joinRule) {

    public record AllowCondition(String roomId,
                                 String type) {

    }
}
