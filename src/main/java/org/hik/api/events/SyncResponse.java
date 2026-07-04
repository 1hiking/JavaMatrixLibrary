package org.hik.api.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

// Author's note: This is the biggest payload to date in the code and probably in the spec. Triple check before doing anything funny... 29/June/2026


public record SyncResponse(AccountData accountData,
                           DeviceLists deviceLists,
                           String nextBatch,
                           Presence presence,
                           Rooms rooms,
                           ToDevice toDevice) {

    public record AccountData(List<Event> events) {
    }

    public record Presence(List<Event> events) {
    }

    public record DeviceLists(List<String> changed,
                              List<String> left) {
    }

    public record ToDevice(List<Event> events) {
    }

    public record Rooms(Map<String, InvitedRoom> invite,
                        Map<String, JoinedRoom> join,
                        Map<String, KnockedRoom> knock,
                        Map<String, LeftRoom> leave) {

        public record InvitedRoom(InviteState inviteState) {
            public record InviteState(List<StrippedStateEvent> events) {
            }
        }

        public record JoinedRoom(AccountData accountData,
                                 Ephemeral ephemeral,
                                 State state,
                                 State stateAfter,
                                 RoomSummary summary,
                                 Timeline timeline,
                                 UnreadNotificationCounts unreadNotifications,
                                 Map<String, ThreadNotificationCounts> unreadThreadNotifications) {

            public record Ephemeral(List<Event> events) {
            }

            public record RoomSummary(@JsonProperty("m.heroes") List<String> mHeroes,
                                      @JsonProperty("m.invited_member_count") Integer mInvitedMemberCount,
                                      @JsonProperty("m.joined_member_count") Integer mJoinedMemberCount) {
            }

            public record UnreadNotificationCounts(Integer highlightCount,
                                                   Integer notificationCount) {
            }

            public record ThreadNotificationCounts(Integer highlightCount,
                                                   Integer notificationCount) {
            }
        }

        public record KnockedRoom(KnockState knockState) {
            public record KnockState(List<StrippedStateEvent> events) {
            }
        }

        public record LeftRoom(AccountData accountData,
                               State state,
                               State stateAfter,
                               Timeline timeline) {
        }
    }


    public record Event(Object content,
                        String type) {
    }

    public record ClientEventWithoutRoomID(Object content,
                                           String eventId,
                                           Long originServerTs,
                                           String sender,
                                           String stateKey,
                                           String type,
                                           UnsignedData unsigned) {

        public record UnsignedData(Long age,
                                   String membership,
                                   Object prevContent,
                                   ClientEventWithoutRoomID redactedBecause,
                                   String transactionId) {
        }
    }

    public record StrippedStateEvent(Object content,
                                     String sender,
                                     String stateKey,
                                     String type) {
    }

    public record State(List<ClientEventWithoutRoomID> events) {
    }

    public record Timeline(List<ClientEventWithoutRoomID> events,
                           Boolean limited,
                           String prevBatch) {
    }
}