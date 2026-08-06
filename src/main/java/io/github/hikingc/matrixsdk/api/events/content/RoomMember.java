package io.github.hikingc.matrixsdk.api.events.content;

import io.github.hikingc.matrixsdk.api.events.ThirdPartyInvite;
import java.net.URI;

public record RoomMember(
    URI avatarUrl,
    String displayname, // according to spec its either String or Null, should it be treated
    // differently? - 31/jul/2026
    Boolean isDirect,
    String joinAuthorizedViaUsersServer,
    String membership,
    String reason,
    ThirdPartyInvite thirdPartyInvite)
    implements StateEventContent {}
// displayname is written without camel-case style
