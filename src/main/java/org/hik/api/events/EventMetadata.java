package org.hik.api.events;


import com.fasterxml.jackson.annotation.JsonProperty;

/// Holds the ID of an event and its timestamp in milliseconds since the Unix epoch.
///
/// @param eventId The ID of the event found.
/// @param originServerTs The event’s timestamp, in milliseconds since the Unix epoch.
///
/// This makes it easy to do a quick comparison to see if the `event_id` fetched is too far out of range to be useful for your use case.
public record EventMetadata(@JsonProperty(required = true) String eventId,
                            @JsonProperty(required = true) long originServerTs) {
}
