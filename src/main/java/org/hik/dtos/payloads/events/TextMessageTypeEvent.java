package org.hik.dtos.payloads.events;

public record TextMessageTypeEvent(
        String msgtype,
        String body
) implements MessageTypeEvent {}
