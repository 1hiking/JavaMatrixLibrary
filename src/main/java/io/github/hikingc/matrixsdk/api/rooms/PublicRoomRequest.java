package io.github.hikingc.matrixsdk.api.rooms;

/// Utility configuration record to pass desired arguments to query the server's public rooms' directory.
///
/// @param filter               to apply in the results.
/// @param includeAllNetworks   whether to include all known networks/protocols from application services on the homeserver.
/// @param limit                of the number of results returned.
/// @param since                a pagination token from a previous request, allowing clients to get the next (or previous) batch of rooms. The direction of pagination is specified solely by which token is supplied, rather than via an explicit flag.
/// @param thirdPartyInstanceId the specific third-party network/protocol to request from the homeserver. Can only be used if `includeAllNetworks` is `false`.
/// @see <a href="https://spec.matrix.org/v1.19/client-server-api/#get_matrixclientv3thirdpartyprotocols"> `/thirdparty/protocols`, the endpoint that returns the instance_id of a `Protocol` Instance</a>
public record PublicRoomRequest(RoomFilter filter,
                                Boolean includeAllNetworks,
                                Integer limit,
                                String since,
                                String thirdPartyInstanceId) {


}
