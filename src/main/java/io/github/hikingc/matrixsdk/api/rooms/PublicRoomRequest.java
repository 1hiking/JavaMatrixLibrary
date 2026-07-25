package io.github.hikingc.matrixsdk.api.rooms;

public record PublicRoomRequest(Filter filter,
                                Boolean includeAllNetworks,
                                Integer limit,
                                String since,
                                String thirdPartyInstanceId) {


}
