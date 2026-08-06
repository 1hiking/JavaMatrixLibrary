package io.github.hikingc.matrixsdk.api.events;

import java.net.URI;

/// Represents the contents of an event
///
/// @param avatarUrl avatar [URI] for this user, if any.
/// @param displayName display name for this user, if any.
/// @param isDirect indicates if the room containing this event was created with the intention of
///   being a direct chat.
/// @param joinAuthorizedViaUsersServer usually found on `join` events, this field is used to denote
///   which homeserver (through representation of a user with sufficient power level) authorized the
///   user’s join.
///
///   Client and server implementations should be aware of the signing implications of including
///   this field in further events: in particular, the event must be signed by the server which owns
///   the user ID in the field. When copying the membership event’s content (for profile updates and
///   similar) it is therefore encouraged to exclude this field in the copy, as otherwise the event
///   might fail event authorization.
/// @param membership the membership state of the user.
/// @param reason optional user-supplied text for why their membership has changed. For kicks and
///   bans, this is typically the reason for the kick or ban. For other membership changes, this is
///   a way for the user to communicate their intent without having to send a message to the room.
///
///   Clients are not recommended to show this reason to users when receiving an invitation due to
///   the potential for spam and abuse. Hiding the reason behind a button or other component is
///   recommended.
/// @param thirdPartyInvite a third-party invite, if this `m.room.member` is the successor to an
///   `m.room.third_party_invite` event.
public record EventContent(
    URI avatarUrl,
    String displayName, // can be null
    Boolean isDirect,
    String joinAuthorizedViaUsersServer,
    String membership, // Some payloads aren't guaranteed to send this...?
    String reason,
    ThirdPartyInvite thirdPartyInvite) {}
