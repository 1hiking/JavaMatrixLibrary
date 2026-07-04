package org.hik.api.rooms;

public record PublicRoomRequest(Filter filter,
                                Boolean includeAllNetworks,
                                Integer limit,
                                String since,
                                String thirdPartyInstanceId) {

    public record Filter(String genericSearchTerm,
                         String roomTypes) {
    }
}
