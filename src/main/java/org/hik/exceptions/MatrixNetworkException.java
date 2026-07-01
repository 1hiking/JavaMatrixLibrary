package org.hik.exceptions;


/// Thrown to indicate that the code has not received a successful HTTP status code.
public class MatrixNetworkException extends RuntimeException {
    /// Constructs a [MatrixNetworkException] with a message.
    ///
    /// @param message The detail message. The detail message is saved for later retrieval by the getMessage() method.
    public MatrixNetworkException(String message) {
        super(message);
    }

    /// Constructs a [MatrixNetworkException] with a message.
    ///
    /// @param message The detail message. The detail message is saved for later retrieval by the getMessage() method.
    /// @param cause   the cause (which is saved for later retrieval by the getCause() method). (A null value is
    /// permitted, and indicates that the cause is nonexistent or unknown.)
    public MatrixNetworkException(String message, Throwable cause) {
        super(message, cause);
    }


}
