package io.github.hikingc.matrixsdk.api.events.types;

import io.github.hikingc.matrixsdk.api.events.SingletonStateEvent;
import io.github.hikingc.matrixsdk.api.events.UnsignedData;

public record RoomGuestAcessEvent(RoomGuestAccess content,
                                  String eventId,
                                  Long originServerTs,
                                  String roomId,
                                  String sender,
                                  UnsignedData unsigned
) implements SingletonStateEvent<RoomGuestAccess> {


    /// @return the type of the event.
    @Override
    public String type() {
        return "m.room.guest_access";
    }


}
