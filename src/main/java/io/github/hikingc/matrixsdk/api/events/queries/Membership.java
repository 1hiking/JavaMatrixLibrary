package io.github.hikingc.matrixsdk.api.events.queries;

/// Membership keys used to filter membership to include or exclude in the request.
public enum Membership {
  /// Filter by `join` value. Users who have this value can send and receive events in the room
  JOIN("join"),
  /// Filter by `invite` value. Users who have this value have been invited but not yet participate.
  INVITE("invite"),
  /// Filter by `knock` value. Users who have this value have requested membership, yet hasn't been
  /// allowed.
  KNOCK("knock"),
  /// Filter by `leave` value. Users who have this value have left the room.
  LEAVE("leave"),
  /// Filter by `banned` value. Users who have this value aren't allowed to join.
  BAN("ban");

  private final String value;

  Membership(String value) {
    this.value = value;
  }

  /// The kind of membership value.
  ///
  /// @return the membership value.
  public String getValue() {
    return this.value;
  }
}
