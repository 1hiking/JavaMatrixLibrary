package org.hik.api.filters;

import java.util.List;

public record FilterDefinition(EventFilter accountData,
                               List<String> eventFields,
                               String eventFormat,
                               EventFilter presence,
                               RoomFilter room) {
}
