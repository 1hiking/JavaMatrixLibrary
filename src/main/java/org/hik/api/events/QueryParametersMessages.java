package org.hik.api.events;

/// This auxiliary record represents the arguments required to format the URI used to query the get /messages endpoint.
///
/// @param from  the token to start returning events from. This token can be obtained from a prev\_batch or
/// next\_batch token returned by the /sync endpoint, or from an end token returned by a previous request to this
/// endpoint.
///
/// This endpoint can also accept a value returned as a start token by a previous request to this endpoint, though
/// servers are not required to support this. Clients should not rely on the behavior.
///
/// If it is not provided, the homeserver shall return a list of messages from the first or last (per the value of
/// the dir parameter) visible event in the room history for the requesting user.
/// @param limit The maximum number of events to return. If not set the default is 10.
/// @param to    The token to stop returning events at. This token can be obtained from a prev\_batch or next\_batch
/// token returned by the /sync endpoint, or from an end token returned by a previous request to this endpoint.
public record QueryParametersMessages(String from, Integer limit, String to) {
    /// Utility instantiation method that notifies the client that no specific queries are required.
    ///
    /// @return An instantiation record with  default query parameters.
    public static QueryParametersMessages defaultParams() {
        return new QueryParametersMessages(null, 10, null);
    }
}
