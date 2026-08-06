package io.github.hikingc.matrixsdk.exceptions;

/// Thrown to indicate that the code has attempted to process an I/O event to which it has failed.
public class MatrixException extends RuntimeException {
  /// Constructs a [MatrixException] with a message.
  ///
  /// @param message The detail message. The detail message is saved for later retrieval by the
  ///   getMessage() method.
  public MatrixException(String message) {
    super(message);
  }

  /// Constructs a [MatrixException] with a message and a throwable.
  ///
  /// @param message the detail message (which is saved for later retrieval by the getMessage()
  ///   method).
  /// @param cause the cause (which is saved for later retrieval by the getCause() method). (A null
  ///   value is permitted, and indicates that the cause is nonexistent or unknown.)
  public MatrixException(String message, Throwable cause) {
    super(message, cause);
  }
}
