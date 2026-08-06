package io.github.hikingc.matrixsdk.api.events.content;

import io.github.hikingc.matrixsdk.api.events.content.roommessages.HasInfo;
import io.github.hikingc.matrixsdk.api.events.content.roommessages.ThumbnailInfo;
import java.net.URI;

public record RoomAvatar(AvatarInfo info, String url)
    implements StateEventContent { // Wanna improve this? Either add the ID factory or "patch" it
  // with URI...

  public record AvatarInfo(
      Integer h,
      String mimetype,
      Integer size,
      ThumbnailInfo thumbnailInfo,
      URI thumbnailUrl,
      Integer w)
      implements HasInfo { // Just like messages/Image, perhaps extract into an interface?
  }
}
