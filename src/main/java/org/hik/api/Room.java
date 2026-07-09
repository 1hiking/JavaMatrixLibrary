package org.hik.api;

import org.hik.api.events.ChronologicalDirectionType;
import org.hik.api.rooms.*;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;

import java.util.List;

/// Core interface for executing Matrix protocol operations against rooms themselves.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see
/// <a href="https://spec.matrix.org/latest/client-server-api/#rooms">Matrix Client-Server API Specification for Rooms</a>
/// @see <a href="https://spec.matrix.org/v1.18/appendices/#room-aliases">Information about the room alias format</a>
public interface Room {
    /// Creates a room, this method will let the homeserver choose the default configuration for most tasks
    /// and the following parameters will overwrite them if set to a non-null value.
    ///
    /// @param isFederated if the room will be federated
    /// @param name        the room's name, if any.
    /// @param aliasName   the room's canonical alias, if any
    /// @param topic       the room's topic, if any.
    /// @param type        the [CreationRoomType]
    /// @param isVisible   if the room will be visible to the public
    /// @return the created room’s ID.
    /// @throws MatrixIOException      when the payload cannot be processed
    /// @throws MatrixNetworkException when the response status is not successful
    String create(boolean isFederated, String name, String aliasName, String topic, CreationRoomType type,
                  boolean isVisible);

    /// Requests the server to resolve a room alias if not possible, the server will use the federation API.
    ///
    /// @param roomAlias the room alias.
    /// @return a [ResolvedAlias] containing the room ids for the requested alias and which servers are aware of it.
    ResolvedAlias resolveAlias(String roomAlias);

    /// Sets a room alias to a room.
    ///
    /// @param roomAlias the room alias.
    /// @param roomId    the room to receive the alias.
    void setAlias(String roomAlias, String roomId);

    /// Requests the server to remove a mapping of a room alias to a room id.
    /// On success, servers might modify `m.room.canonical_alias`
    ///
    /// @param roomAlias the room alias to remove.
    void deleteAlias(String roomAlias);

    /// Requests a list of aliases maintained by the local server for the given room, requires to be part of the room
    /// unless it is configured to be world readable.
    ///
    /// Spec Note: Don't use this endpoint to display data as it is not curated, use data from `m.room.canonical_alias`.
    ///
    /// @param roomAlias the room ID to find local aliases of.
    /// @return a list of aliases contained in a [RoomAliases] record.
    RoomAliases getAliasesOfARoom(String roomAlias);

    /// Requests the server to retrieve a list of the user's current rooms (in simple terms whoever calls this method).
    ///
    /// @return a list of the rooms in a [JoinedRooms] record.
    JoinedRooms getJoinedRooms();

    /// Send an invitation to a user to participate in a room, this endpoint requires the caller to be a member of said
    /// room to invite other users.
    ///
    /// @param roomId the target room ID.
    /// @param event  a [RoomMembershipRequest] with the appropriate information.
    /// @see <a href="https://spec.matrix.org/v1.18/client-server-api/#third-party-invites">third-party invites spec</a>
    ///  for another type of invitation.
    void inviteUser(String roomId, RoomMembershipRequest event);

    /// If allowed, it starts participation in a room.
    ///
    /// @param roomIdOrAlias the target room, either the ID or Alias.
    /// @param request       a [JoinRoomRequest] where additional information can be passed.
    /// @param via           the servers to attempt to join the room through. One of the servers must be
    /// participating in the room.
    /// @return the room ID
    String joinByRoomIdOrAliasIfAllowed(String roomIdOrAlias, JoinRoomRequest request, List<String> via);

    /// If allowed, it starts participation in a room.
    ///
    /// @param roomId  the target room ID.
    /// @param request a [JoinRoomRequest] where additional information can be passed.
    /// @param via     the servers to attempt to join the room through. One of the servers must be
    /// participating in the room.
    /// @return the room ID
    String joinByRoomIdIfAllowed(String roomId, JoinRoomRequest request, List<String> via);

    /// Knock on a room to ask for permission to join. Acceptance of this request happens out of band.
    ///
    /// @param roomIdOrAlias the target room, either the ID or Alias.
    /// @param reason        an optional reason to include in the event.
    /// @param via           the servers to attempt to join the room through. One of the servers must be
    /// participating in the room.
    /// @return the room ID of the knocked room.
    String knockOn(String roomIdOrAlias, String reason, List<String> via);

    /// Sends a request to leave the room, upon success, you will forget all messages from this room.
    /// If all users on a room forget it, the room is eligible for deletion.
    /// You must [forget](#forget(String)) the room first before calling this method.
    ///
    /// @param roomId the target room ID.
    /// @throws MatrixIOException      when the payload cannot be processed.
    /// @throws MatrixNetworkException when the response status is not successful.
    void forget(String roomId);

    /// Sends a request to leave the room, upon success, you will no longer receive new messages from this room.
    /// If the user was invited to the room, but had not joined, this call serves to reject the invite.
    /// Some servers MAY additionally `forget` the room when leaving.
    ///
    /// @param roomId the target room ID.
    /// @throws MatrixIOException      when the payload cannot be processed
    /// @throws MatrixNetworkException when the response status is not successful
    void leave(String roomId);

    /// Sends a request to kick someone from a room. Caller must have a configured power level to perform this
    /// operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    void kick(String roomId, RoomMembershipRequest event);

    /// Sends a request to ban someone from a room. Caller must have a configured power level to perform this operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    void ban(String roomId, RoomMembershipRequest event);

    /// Sends a request to unban someone from a room. Caller must have a configured power level to perform this
    /// operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    void unban(String roomId, RoomMembershipRequest event);

    /// Gets the visibility of a given room in the server’s published room directory.
    /// Authentication is not required to run this request.
    /// NOTE: This does NOT guarantee join rules are public.
    ///
    /// @param roomId the target room ID.
    /// @return a [String] with the room visibility.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    String getRoomDirectoryVisibilityType(String roomId);

    /// Sets the visibility of a given room in the server’s published room directory.
    ///
    /// @param roomId   the target room ID.
    /// @param roomType a [VisibilityRoomType] with the room visibility type
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    void setRoomDirectoryVisibilityType(String roomId, VisibilityRoomType roomType);

    /// Lists a server’s published room directory.
    ///
    /// @param limit  limit of records to show
    /// @param server what server to fetch from, if not supplied it will fetch the local server. Case-sensitive.
    /// @param since  a pagination token from a previous request, allowing you to get the next or previous
    /// batch of rooms. The direction of pagination is specified by which token is supplied (not like
    ///  [ChronologicalDirectionType]).
    /// @return a [PublicRoomDirectory] containing [PublishedRoomsChunk] records of the published rooms on the server.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    PublicRoomDirectory getPublishedRoomDirectory(Integer limit, String server, String since);

    /// Lists a server’s published room directory.
    ///
    /// @param request a [PublicRoomRequest] with additional filters for the request.
    /// @return a [PublicRoomDirectory] containing [PublishedRoomsChunk] records of the published rooms on the server.
    PublicRoomDirectory getPublishedRoomDirectory(PublicRoomRequest request);

    /// Retrieves a summary for a room. The response data might yield outdated, partial or even no data.
    ///
    /// @param roomIdOrAlias the room id or alias of the room to target
    /// @param via           the servers to attempt to request the summary from when the local server cannot generate it
    /// @return a [RoomSummary] containing all the information about the room.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    RoomSummary getRoomSummary(String roomIdOrAlias, List<String> via);
}
