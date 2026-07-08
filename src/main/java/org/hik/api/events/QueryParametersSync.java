package org.hik.api.events;

/// This record represents the arguments required to format the URI used to query the get /sync endpoint.
///
/// @param filter        The ID of a filter created using the filter API or a filter JSON object encoded as a string.
/// @param fullState     Controls whether to include the full state for all rooms the user is a member of. The
/// default is false.
/// @param setPresence   Controls whether this client is automatically marked as online by polling this API. If this
/// parameter is omitted then this client is automatically marked as online when it uses this API. Otherwise, if the
/// parameter is set to `offline` then the client is not marked as being online when it uses this API. When set to
/// `unavailable`, the client is marked as being idle.
/// @param since         A point in time to continue a sync from. This should be the `next_batch` token returned by
/// an earlier call to this endpoint.
/// @param timeout       The maximum time to wait, in milliseconds, before returning this request. Default is 0
/// @param useStateAfter Controls whether to receive state changes between the **previous sync and the start of the
/// timeline**, or **between the previous sync and the end of the timeline**.
/// @see
/// <a href="https://spec.matrix.org/v1.18/client-server-api/#filtering">Filter spec details for more information about filtering</a>
public record QueryParametersSync(String filter,
                                  Boolean fullState,
                                  String setPresence,
                                  String since,
                                  Integer timeout,
                                  Boolean useStateAfter) {
}
