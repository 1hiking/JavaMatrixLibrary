package org.hik.api.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;


// If we make an interface for all message content types we can get rid of these Objects and make serialization more
// performant with the addition of type safety
// 7/July/2026

/// Represents the response body of a `/sync` request against a Matrix homeserver.
///
/// This is the top-level payload returned when polling for account, room, presence, and device updates.
///
/// @param accountData global account data not scoped to a specific room.
/// @param deviceLists devices that changed or left the sync scope, used for end-to-end encryption tracking.
/// @param nextBatch   the batch token to supply as `since` on the next sync request.
/// @param presence    presence updates for users the client is tracking.
/// @param rooms       updates for rooms the client is invited to, joined in, knocking on, or has left.
/// @param toDevice    to-device messages sent directly to this client.
public record Sync(AccountData accountData,
                   DeviceLists deviceLists,
                   String nextBatch,
                   Presence presence,
                   Rooms rooms,
                   ToDevice toDevice) {

    /// A batch of account-data events.
    ///
    /// @param events the account data events.
    public record AccountData(List<Event> events) {
    }

    /// A batch of presence events.
    ///
    /// @param events the presence events.
    public record Presence(List<Event> events) {
    }

    /// Devices whose identity or cross-signing keys changed, relevant for E2EE.
    ///
    /// @param changed device IDs of users whose devices have changed.
    /// @param left    device IDs of users no longer sharing an encrypted room with this client.
    public record DeviceLists(List<String> changed,
                              List<String> left) {
    }

    /// A batch of to-device events.
    ///
    /// @param events the to-device events.
    public record ToDevice(List<Event> events) {
    }

    /// Room updates grouped by the client's membership state in each room.
    ///
    /// @param invite rooms the client has been invited to, keyed by room ID.
    /// @param join   rooms the client is currently joined to, keyed by room ID.
    /// @param knock  rooms the client is knocking on, keyed by room ID.
    /// @param leave  rooms the client has left or been banned from, keyed by room ID.
    public record Rooms(Map<String, InvitedRoom> invite,
                        Map<String, JoinedRoom> join,
                        Map<String, KnockedRoom> knock,
                        Map<String, LeftRoom> leave) {

        /// A room the client has been invited to but not yet joined.
        ///
        /// @param inviteState the stripped state events describing the room prior to joining.
        public record InvitedRoom(InviteState inviteState) {

            /// Stripped state for an invited room.
            ///
            /// @param events the stripped state events.
            public record InviteState(List<StrippedStateEvent> events) {
            }
        }

        /// A room the client is currently joined to.
        ///
        /// @param accountData               account data scoped to this room.
        /// @param ephemeral                 ephemeral events for this room, e.g. typing notifications.
        /// @param state                     the full room state.
        /// @param stateAfter                the room state after the timeline events in this response are applied.
        /// @param summary                   a summary of the room, e.g. heroes and member counts.
        /// @param timeline                  the timeline of events for this room.
        /// @param unreadNotifications       unread notification counts for this room.
        /// @param unreadThreadNotifications unread notification counts per thread, keyed by thread root event ID.
        public record JoinedRoom(AccountData accountData,
                                 Ephemeral ephemeral,
                                 State state,
                                 @JsonProperty(required = true) State stateAfter,
                                 RoomSummary summary,
                                 Timeline timeline,
                                 UnreadNotificationCounts unreadNotifications,
                                 Map<String, ThreadNotificationCounts> unreadThreadNotifications) {

            /// A batch of ephemeral events for a joined room.
            ///
            /// @param events the ephemeral events.
            public record Ephemeral(List<Event> events) {
            }

            /// Summary information about a room, used to render it without loading full state.
            ///
            /// @param mHeroes             a list of user IDs representative of the room's other members.
            /// @param mInvitedMemberCount the number of users with `invite` membership.
            /// @param mJoinedMemberCount  the number of users with `join` membership.
            public record RoomSummary(@JsonProperty("m.heroes") List<String> mHeroes,
                                      @JsonProperty("m.invited_member_count") Integer mInvitedMemberCount,
                                      @JsonProperty("m.joined_member_count") Integer mJoinedMemberCount) {
            }

            /// Unread notification counts for a room.
            ///
            /// @param highlightCount    the number of unread notifications that are highlights.
            /// @param notificationCount the total number of unread notifications.
            public record UnreadNotificationCounts(Integer highlightCount,
                                                   Integer notificationCount) {
            }

            /// Unread notification counts scoped to a single thread.
            ///
            /// @param highlightCount    the number of unread notifications in this thread that are highlights.
            /// @param notificationCount the total number of unread notifications in this thread.
            public record ThreadNotificationCounts(Integer highlightCount,
                                                   Integer notificationCount) {
            }
        }

        /// A room the client is knocking on.
        ///
        /// @param knockState the stripped state events describing the room prior to the knock being accepted.
        public record KnockedRoom(KnockState knockState) {

            /// Stripped state for a knocked room.
            ///
            /// @param events the stripped state events.
            public record KnockState(List<StrippedStateEvent> events) {
            }
        }

        /// A room the client has left or been banned from.
        ///
        /// @param accountData account data scoped to this room, as of leaving.
        /// @param state       the room state at the point the client stopped receiving updates.
        /// @param stateAfter  the room state after the timeline events in this response are applied.
        /// @param timeline    the timeline of events leading up to the client leaving the room.
        public record LeftRoom(AccountData accountData,
                               State state,
                               State stateAfter,
                               Timeline timeline) {
        }
    }

    /// A minimal event with no room, sender, or metadata context — used for account data,
    /// presence, and to-device events.
    ///
    /// @param content the event content, shape depends on `type`.
    /// @param type    the event type, e.g. `m.typing`.
    public record Event(@JsonProperty(required = true) Object content,
                        @JsonProperty(required = true) String type) {
    }

    /// A client-facing event as it appears in room timelines and state, excluding the room ID
    /// (since it is already known from context).
    ///
    /// @param content        the event content, shape depends on `type`.
    /// @param eventId        the globally unique event identifier.
    /// @param originServerTs the timestamp, in milliseconds since the Unix epoch, when this event was sent.
    /// @param sender         the user ID of the event's sender.
    /// @param stateKey       the state key, present only if this is a state event.
    /// @param type           the event type, e.g. `m.room.message`.
    /// @param unsigned       additional metadata not covered by the event's signature.
    public record ClientEventWithoutRoomID(Object content,
                                           String eventId,
                                           Long originServerTs,
                                           String sender,
                                           String stateKey,
                                           String type,
                                           UnsignedData unsigned) {

        /// Additional, unsigned metadata about an event.
        ///
        /// @param age             the time in milliseconds since this event was sent.
        /// @param membership      the sender's membership at the time of this event, if applicable.
        /// @param prevContent     the previous content for this state key, if this is a state event replacing one.
        /// @param redactedBecause the redaction event responsible for redacting this event, if it was redacted.
        /// @param transactionId   the transaction ID set by the sending client, if the current client was the sender.
        public record UnsignedData(Long age,
                                   String membership,
                                   Object prevContent,
                                   ClientEventWithoutRoomID redactedBecause,
                                   String transactionId) {
        }
    }

    /// A reduced-detail state event included in invite or knock previews, omitting fields such
    /// as `event_id` and timestamps that are not part of the stripped-state contract.
    ///
    /// @param content  the event content, shape depends on `type`.
    /// @param sender   the user ID of the event's sender.
    /// @param stateKey the state key for this event.
    /// @param type     the event type, e.g. `m.room.member`.
    public record StrippedStateEvent(@JsonProperty(required = true) Object content,
                                     @JsonProperty(required = true) String sender,
                                     @JsonProperty(required = true) String stateKey,
                                     @JsonProperty(required = true) String type) {
    }

    /// A batch of room state events.
    ///
    /// @param events the state events.
    public record State(List<ClientEventWithoutRoomID> events) {
    }

    /// A paginated batch of timeline events for a room.
    ///
    /// @param events    the timeline events, in chronological order.
    /// @param limited   whether the timeline was truncated, requiring further pagination to retrieve earlier events.
    /// @param prevBatch a pagination token for retrieving events older than this batch.
    public record Timeline(List<ClientEventWithoutRoomID> events,
                           Boolean limited,
                           String prevBatch) {
    }
}