package io.github.hikingc.matrixsdk.api.events.content;

import java.util.List;

public record RoomPinnedEvents(List<String> pinned)
    implements StateEventContent { // Totally not gonna cause confusion
}
