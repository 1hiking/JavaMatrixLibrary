package io.github.hikingc.matrixsdk.api.events.states;

public record RoomJoinRules(AllowCondition allow,
                           String joinRule) {

    public record AllowCondition(String roomId,
                                 String type) {

    }
}
