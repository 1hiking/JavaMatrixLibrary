package org.hik.api.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;


/// Interface that enforces fields required by all state events.
///
/// @param <T> the content
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public interface RoomStateEvent<T> {

    T content();

    String eventId();

    Long originServerTs();

    String roomId();

    String sender();

    String stateKey();

    String type();

    List<Map<String, Object>> unsigned(); //TODO WRONG

}
