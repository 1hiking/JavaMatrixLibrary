package org.hik.constants;

public enum EventType {
    IMAGE("m.image"),
    FILE("m.file"),
    AUDIO("m.audio");

    public final String type;

    EventType(String type) {
        this.type = type;
    }

}
