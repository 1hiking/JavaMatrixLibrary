package org.hik.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @param errCode A specification defined error code.
 * @param error   A human-readable error message.
 */
public record ErrorResponse(@JsonProperty("errcode") String errCode,
                            String error) {
}
