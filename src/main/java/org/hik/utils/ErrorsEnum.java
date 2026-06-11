package org.hik.utils;

/**
 * @deprecated The reason this is deprecated is that {@link org.hik.dtos.responses.ErrorResponse} accomplishes a better task for informing the user what error is occurring
 */
@Deprecated()
public enum ErrorsEnum {
    M_UNRECOGNIZED(400),
    M_FORBIDDEN(403),
    M_UNKNOWN_TOKEN(401),
    M_BAD_JSON(400),
    M_NOT_FOUND(404),
    M_LIMIT_EXCEEDED(429);

    private final int httpStatusCode;

    ErrorsEnum(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }
}