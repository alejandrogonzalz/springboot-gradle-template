package com.example.backend.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Raw request DTO to capture audit log filter parameters from GET or POST. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit log search request parameters")
public class AuditLogFilterRequest {

  @Schema(description = "Filter by username (partial match)", example = "admin")
  private String username;

  @Schema(
      description = "Filter by operation type (e.g., CREATE_USER, LOGIN)",
      example = "UPDATE_USER")
  private String operation;

  @Schema(description = "Filter by affected entity type", example = "Product")
  private String entityType;

  @Schema(description = "Filter by specific entity ID", example = "550")
  private Long entityId;

  @Schema(description = "Filter by origin IP address", example = "192.168.1.1")
  private String ipAddress;

  @Schema(description = "Filter by request URI path", example = "/api/v1/auth/login")
  private String requestUri;

  @Schema(description = "Filter by HTTP method", example = "DELETE")
  private String httpMethod;

  @Schema(description = "Filter by operation success status", example = "true")
  private Boolean success;

  @Schema(description = "Filter by multiple usernames", example = "[\"manager1\", \"clerk2\"]")
  private List<String> usernames;

  @Schema(description = "Filter by multiple operation types", example = "[\"CREATE\", \"DELETE\"]")
  private List<String> operations;

  @Schema(description = "Filter by multiple entity types", example = "[\"Order\", \"Inventory\"]")
  private List<String> entityTypes;

  @Schema(
      description = "Creation date from (ISO-8601: yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss)",
      example = "2024-01-01")
  private String createdAtFrom;

  @Schema(
      description = "Creation date to (ISO-8601: yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss)",
      example = "2024-12-31")
  private String createdAtTo;
}
