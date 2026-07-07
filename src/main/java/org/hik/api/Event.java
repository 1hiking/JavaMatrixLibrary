package org.hik.api;

import org.hik.api.events.*;
import org.hik.exceptions.MatrixIOException;
import org.hik.exceptions.MatrixNetworkException;

import java.nio.file.Path;

/// Core interface for executing Matrix protocol operations against room event streams.
///
/// All operations in this interface are blocking. Implementations must ensure
/// thread safety and avoid synchronization blocks that cause carrier thread pinning
/// during network I/O.
///
/// @see
/// <a href="https://spec.matrix.org/latest/client-server-api/#events">Matrix Client-Server API Specification for Events</a>
public interface Event {
    /// Creates a `m.room.message`event to a Matrix room.
    ///
    /// @param roomId      the id of the room to post the event.
    /// @param matrixEvent a well constructed [MatrixEvent].
    /// @return a [String] representing a unique identifier of the event.
    /// @throws MatrixIOException      when the payload cannot be processed.
    /// @throws MatrixNetworkException when the response status is not successful.
    String publishRoomMessage(String roomId, MatrixEvent matrixEvent);

    /// Synchronously uploads a local multimedia resource to the Matrix media server.
    ///
    /// @param resource the local path of the resource to upload.
    /// @return a [String] containing the MXC URI string upon successful upload.
    String uploadResource(Path resource);

    /// Sends a `/sync` request, this method is not responsible for any type of HTTP Polling.
    ///
    /// @param params the [QueryParametersSync] for the query, not all are required.
    /// @return a [SyncResponse] with all the corresponding information.
    /// @throws MatrixIOException      when the payload cannot be processed.
    /// @throws MatrixNetworkException when the response status is not successful.
    SyncResponse sync(QueryParametersSync params);


    /// Returns a list of message and state events for a room. It uses pagination query parameters to paginate
    /// history in the room.
    /// The content is not parsed or escaped which means newlines (\n) and such escape sequences will not be parsed.
    ///
    /// @param roomId the target room ID.
    /// @param params the [QueryParametersMessages] for the operation.
    /// @param dir    the [ChronologicalDirectionType] to return events from.
    /// @return a [Messages] record with the available data.
    /// @throws MatrixIOException    when the payload cannot be processed.
    /// @throws NullPointerException when the roomId is null.
    Messages getListOfMessages(String roomId, ChronologicalDirectionType dir, QueryParametersMessages params);
}
