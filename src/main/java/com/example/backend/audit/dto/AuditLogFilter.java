package com.example.backend.audit.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter DTO for audit log queries with optional filters.
 *
 * <p>Used with {@link com.example.backend.common.specification.SpecificationBuilder} to build
 * complex queries dynamically.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogFilter {

  /** Filter by username */
  private String username;

  /** Filter by operation type (e.g., "CREATE_USER", "UPDATE_USER") */
  private String operation;

  /** Filter by entity type (e.g., "User", "Product") */
  private String entityType;

  /** Filter by entity ID */
  private Long entityId;

  /** Filter by IP address */
  private String ipAddress;

  /** Filter by request URI */
  private String requestUri;

  /** Filter by HTTP method (GET, POST, PUT, DELETE) */
  private String httpMethod;

  /** Filter by success status */
  private Boolean success;

  /** Filter by creation date from (inclusive) */
  private Instant createdAtFrom;

  /** Filter by creation date to (inclusive) */
  private Instant createdAtTo;

  /** Filter by multiple usernames */
  private List<String> usernames;

  /** Filter by multiple operations */
  private List<String> operations;

  /** Filter by multiple entity types */
  private List<String> entityTypes;
}
