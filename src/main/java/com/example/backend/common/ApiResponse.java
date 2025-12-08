package com.example.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper.
 *
 * <p>Provides a consistent response structure across all endpoints.
 *
 * @param <T> the type of data in the response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API response wrapper")
public class ApiResponse<T> {

  @Schema(description = "Indicates if the request was successful", example = "true")
  @Builder.Default
  private Boolean success = true;

  @Schema(description = "Response message", example = "Operation completed successfully")
  private String message;

  @Schema(description = "Response data")
  private T data;

  @Schema(description = "Response timestamp", example = "2024-01-01T10:00:00Z")
  @Builder.Default
  private Instant timestamp = Instant.now();

  /**
   * Creates a successful response with data.
   *
   * @param data the response data
   * @param <T> the type of data
   * @return ApiResponse instance
   */
  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder().success(true).data(data).timestamp(Instant.now()).build();
  }

  /**
   * Creates a successful response with data and message.
   *
   * @param data the response data
   * @param message the response message
   * @param <T> the type of data
   * @return ApiResponse instance
   */
  public static <T> ApiResponse<T> success(T data, String message) {
    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .timestamp(Instant.now())
        .build();
  }

  /**
   * Creates an error response with message.
   *
   * @param message the error message
   * @param <T> the type of data
   * @return ApiResponse instance
   */
  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder()
        .success(false)
        .message(message)
        .timestamp(Instant.now())
        .build();
  }
}
