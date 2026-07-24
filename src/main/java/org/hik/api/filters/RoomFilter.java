package org.hik.api.filters;

import java.util.List;

public record RoomFilter(RoomEventFilter accountData,
                         RoomEventFilter ephemeral,
                         Boolean includeLeave,
                         List<String> notRooms,
                         List<String> rooms,
                         RoomEventFilter state,
                         RoomEventFilter timeline) {
}
