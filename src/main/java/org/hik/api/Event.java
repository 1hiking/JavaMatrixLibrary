package org.hik.api;

import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;
import org.hik.payloads.roomevents.ChronologicalDirectionType;
import org.hik.payloads.roomevents.MatrixEvent;
import org.hik.payloads.roomevents.Messages;
import org.hik.payloads.roomevents.QueryParametersMessages;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/// Core interface for executing Matrix protocol operations against room event streams.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see <a href="https://spec.matrix.org/latest/client-server-api/#events">Matrix Client-Server API Specification for Events</a>
    public interface Event {
    /// Asynchronously requests the posting of a message to a Matrix room.
    ///
    /// @param roomId      the id of the room to post the event
    /// @param matrixEvent a well constructed [MatrixEvent]
    /// @return A [CompletableFuture] with a [String] representing a unique identifier of the event
    /// @throws MatrixIOException      when the payload cannot be processed
    /// @throws MatrixNetworkException when the response status is not successful
    /// @throws InterruptedException   when the HTTP Client is interrupted
    String publishRoomMessage(String roomId, MatrixEvent matrixEvent) throws InterruptedException;

    /// Synchronously uploads a local multimedia resource to the Matrix media server.
    ///
    /// @param resource the local path of the resource to upload
    /// @return A [String] containing the MXC URI string upon successful upload.
    /// @throws InterruptedException when the HTTP Client is interrupted
    String uploadResource(Path resource) throws InterruptedException;

    void doSyncPoll() throws InterruptedException;

    /// Returns a list of message and state events for a room. It uses pagination query parameters to paginate
    /// history in the room.
    /// The content is not parsed or escaped which means newlines (\n) and such escape sequences will not be parsed.
    ///
    /// @param roomId the target room ID.
    /// @param params the [QueryParametersMessages] for the operation
    /// @param dir    the [ChronologicalDirectionType] to return events from.
    /// @return A [CompletableFuture] containing a [Messages] record with the messages from the room
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws InterruptedException when the HTTP Client is interrupted
    /// @throws NullPointerException when the roomId is null.
    Messages getListOfMessages(String roomId, ChronologicalDirectionType dir, QueryParametersMessages params) throws InterruptedException;
}
