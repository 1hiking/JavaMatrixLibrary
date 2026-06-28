package org.hik.exceptions;

import com.fasterxml.jackson.annotation.JsonProperty;

/// The record used by all endpoints when the operation is not successful.
///
/// @param errCode A specification defined error code.
/// @param error   A human-readable error message.
public record ErrorResponse(@JsonProperty("errcode") String errCode,
                            String error) {
}
