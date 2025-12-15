package com.example.backend.exception;

/**
 * Exception thrown when a user is not authenticated or authentication is required.
 *
 * <p>Results in a 401 UNAUTHORIZED HTTP response.
 */
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }
}
