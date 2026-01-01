package com.example.backend.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for AuditLog responses.
 *
 * <p>Exposes audit trail information in a clean, documented format for API consumers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Audit log entry representing a tracked operation")
public class AuditLogDto {

  @Schema(description = "Unique audit log ID", example = "1")
  private Long id;

  @Schema(description = "Username of the user who performed the action", example = "admin")
  private String username;

  @Schema(
      description = "Type of operation performed",
      example = "CREATE_USER",
      allowableValues = {
        "CREATE_USER",
        "UPDATE_USER",
        "DELETE_USER",
        "RESTORE_USER",
        "CREATE_PRODUCT",
        "UPDATE_PRODUCT",
        "DELETE_PRODUCT"
      })
  private String operation;

  @Schema(
      description = "Type of entity affected",
      example = "User",
      allowableValues = {"User", "Product", "Order"})
  private String entityType;

  @Schema(description = "ID of the affected entity", example = "123")
  private Long entityId;

  @Schema(description = "Human-readable description of the action", example = "User profile update")
  private String description;

  @Schema(description = "IP address of the request", example = "192.168.1.100")
  private String ipAddress;

  @Schema(description = "Request URI", example = "/api/v1/users/123")
  private String requestUri;

  @Schema(
      description = "HTTP method",
      example = "POST",
      allowableValues = {"GET", "POST", "PUT", "DELETE", "PATCH"})
  private String httpMethod;

  @Schema(
      description = "JSON representation of changes (before/after state)",
      example = "{\"request\":{\"username\":\"john\"},\"response\":{\"id\":123}}")
  private String changes;

  @Schema(description = "Additional metadata in JSON format", example = "{\"executionTime\":45}")
  private String metadata;

  @Schema(description = "Whether the operation was successful", example = "true")
  private Boolean success;

  @Schema(
      description = "Error message if operation failed",
      example = "Validation error: email already exists")
  private String errorMessage;

  @Schema(
      description = "Timestamp when the audit log was created",
      example = "2024-12-31T23:00:00Z")
  private Instant createdAt;

  @Schema(
      description = "Timestamp when the audit log was last updated",
      example = "2024-12-31T23:00:00Z")
  private Instant updatedAt;
}
