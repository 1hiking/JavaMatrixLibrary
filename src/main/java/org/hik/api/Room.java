package org.hik.api;

import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import org.hik.payloads.roomevents.ChronologicalDirectionType;
import org.hik.payloads.roomstate.*;

import java.net.URI;
/// Core interface for executing Matrix protocol operations against rooms themselves.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see <a href="https://spec.matrix.org/latest/client-server-api/#rooms">Matrix Client-Server API Specification for Rooms</a>
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
    /// @throws InterruptedException   when the HTTP Client is interrupted
    String create(boolean isFederated, String name, String aliasName, String topic, CreationRoomType type,
                  boolean isVisible) throws InterruptedException;

    /// Sends a request to leave the room, upon success, you will forget all messages from this room.
    /// If all users on a room forget it, the room is eligible for deletion.
    /// You must [forget](#forget(String)) the room first before calling this method.
    ///
    /// @param roomId the target room ID.
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException      when the payload cannot be processed.
    /// @throws MatrixNetworkException when the response status is not successful.
    /// @throws InterruptedException   when the HTTP Client is interrupted.
    boolean forget(String roomId) throws InterruptedException;

    /// Sends a request to leave the room, upon success, you will no longer receive new messages from this room.
    /// If the user was invited to the room, but had not joined, this call serves to reject the invite.
    /// Some servers MAY additionally `forget` the room when leaving.
    ///
    /// @param roomId the target room ID.
    /// @return true if the roomId was set and the request finished with no issue.
    /// @throws MatrixIOException      when the payload cannot be processed
    /// @throws MatrixNetworkException when the response status is not successful
    /// @throws InterruptedException   when the HTTP Client is interrupted
    boolean leave(String roomId) throws InterruptedException;

    /// Sends a request to kick someone from a room. Caller must have a configured power level to perform this
    /// operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
    boolean kick(String roomId, RoomMembershipRequest event) throws InterruptedException;

    /// Sends a request to ban someone from a room. Caller must have a configured power level to perform this operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
    boolean ban(String roomId, RoomMembershipRequest event) throws InterruptedException;

    /// Sends a request to unban someone from a room. Caller must have a configured power level to perform this
    /// operation.
    ///
    /// @param roomId the target room ID.
    /// @param event  the body to supply the request.
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
    boolean unban(String roomId, RoomMembershipRequest event) throws InterruptedException;

    /// Gets the visibility of a given room in the server’s published room directory.
    /// Authentication is not required to run this request.
    /// NOTE: This does NOT guarantee join rules are public.
    ///
    /// @param roomId the target room ID.
    /// @return a [String] with the room visibility.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
    String getRoomDirectoryVisibilityType(String roomId) throws InterruptedException;

    /// Sets the visibility of a given room in the server’s published room directory.
    ///
    /// @param roomId   the target room ID.
    /// @param roomType a [VisibilityRoomType] with the room visibility type
    /// @return `true` if the request finished with no issue.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
    boolean setRoomDirectoryVisibilityType(String roomId, VisibilityRoomType roomType) throws InterruptedException;

    /// Lists a server’s published room directory.
    ///
    /// @param limit  limit of records to show
    /// @param server what server to fetch from, if not supplied it will fetch the local server. Case-sensitive.
    /// @param since  a pagination token from a previous request, allowing you to get the next or previous
    /// batch of rooms. The direction of pagination is specified by which token is supplied (not like
    ///  [ChronologicalDirectionType]).
    /// @return a [PublicRoomDirectory] containing [PublishedRoomsChunk] records of the published rooms on the server.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
    PublicRoomDirectory getPublishedRoomDirectory(Integer limit, String server, String since) throws InterruptedException;

    /// Retrieves a summary for a room. The response data might yield outdated, partial or even no data.
    ///
    /// @param roomIdOrAlias the room id or alias of the room to target
    /// @param via           the servers to attempt to request the summary from when the local server cannot generate it
    /// @return a [RoomSummary] containing all the information about the room.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted.
    /// @throws NullPointerException when the roomId is null.
    RoomSummary getRoomSummary(String roomIdOrAlias, URI via) throws InterruptedException;
}
