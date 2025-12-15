package com.example.backend.exception;

import com.example.backend.common.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for REST API.
 *
 * <p>Provides centralized exception handling across all controllers, ensuring consistent error
 * responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles ResourceNotFoundException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with 404 status
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
      ResourceNotFoundException ex, WebRequest request) {
    log.error("Resource not found: {}", ex.getMessage());

    ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  /**
   * Handles DuplicateResourceException.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with 409 status
   */
  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(
      DuplicateResourceException ex, WebRequest request) {
    log.error("Duplicate resource: {}", ex.getMessage());

    ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  /**
   * Handles validation errors from @Valid annotation.
   *
   * @param ex the exception
   * @return error response with 400 status and field errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    log.error("Validation error: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    ApiResponse<Map<String, String>> response =
        ApiResponse.<Map<String, String>>builder()
            .success(false)
            .message("Validation failed")
            .data(errors)
            .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles constraint violation exceptions.
   *
   * @param ex the exception
   * @return error response with 400 status
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolation(
      ConstraintViolationException ex) {
    log.error("Constraint violation: {}", ex.getMessage());

    Map<String, String> errors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    ConstraintViolation::getMessage));

    ApiResponse<Map<String, String>> response =
        ApiResponse.<Map<String, String>>builder()
            .success(false)
            .message("Constraint violation")
            .data(errors)
            .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles illegal argument exceptions.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with 400 status
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    log.error("Illegal argument: {}", ex.getMessage());

    ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles UnauthorizedException (custom 401 errors).
   *
   * @param ex the exception
   * @return error response with 401 status
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
    log.warn("Unauthorized access: {}", ex.getMessage());

    ApiResponse<Void> response = ApiResponse.error(ex.getMessage());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles unauthorized exception.
   *
   * @param ex the exception
   * @return error response with 401 status
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleAuthenticationException(
      BadCredentialsException ex) {
    ApiResponse<Map<String, String>> response =
        ApiResponse.<Map<String, String>>builder().success(false).message(ex.getMessage()).build();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /**
   * Handles missing required cookie exceptions.
   *
   * @param ex the exception
   * @return error response with 401 status
   */
  @ExceptionHandler(MissingRequestCookieException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingCookieException(
      MissingRequestCookieException ex) {
    log.warn("Missing required cookie: {}", ex.getCookieName());

    ApiResponse<Void> response =
        ApiResponse.error("Authentication required. Please log in to access this resource.");
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  /**
   * Handles Spring Security access denied exceptions with detailed information.
   *
   * <p>Provides information about:
   *
   * <ul>
   *   <li>The user who was denied access
   *   <li>Their current authorities
   *   <li>The action they attempted
   * </ul>
   *
   * @param ex the exception
   * @return error response with 403 status
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleAccessDeniedException(
      AccessDeniedException ex) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // Check if user is actually authenticated
    boolean isAuthenticated =
        auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
    if (!isAuthenticated) {
      log.warn("Access denied - User not authenticated");

      ApiResponse<Map<String, String>> response =
          ApiResponse.<Map<String, String>>builder()
              .success(false)
              .message("Authentication required. Please log in to access this resource.")
              .build();
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // User is logged in but lacks permission
    String username = auth.getName();
    String authorities = auth.getAuthorities().toString();

    log.warn(
        "Access denied for authenticated user '{}' with authorities: {}", username, authorities);

    Map<String, String> details = new HashMap<>();
    details.put("user", username);
    details.put("authorities", authorities);

    ApiResponse<Map<String, String>> response =
        ApiResponse.<Map<String, String>>builder()
            .success(false)
            .message("Access denied. You do not have the required permissions for this operation.")
            .data(details)
            .build();
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /**
   * Handles all other exceptions.
   *
   * @param ex the exception
   * @param request the web request
   * @return error response with 500 status
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex, WebRequest request) {
    log.error("Unexpected error occurred", ex);

    ApiResponse<Void> response =
        ApiResponse.error("An unexpected error occurred. Please try again later.");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
