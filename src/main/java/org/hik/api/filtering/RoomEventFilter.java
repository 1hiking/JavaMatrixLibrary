package org.hik.api.filtering;

import java.util.List;

public record RoomEventFilter(Boolean containsUrl,
                              Boolean includeRedundantMembers,
                              Boolean lazyLoadMembers,
                              Integer limit,
                              List<String> notRooms,
                              List<String> notSenders,
                              List<String> notTypes,
                              List<String> rooms,
                              List<String> senders,
                              List<String> types,
                              Boolean unreadThreadNotifications) {
}
