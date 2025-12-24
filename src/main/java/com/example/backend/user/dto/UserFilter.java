package com.example.backend.user.dto;

import com.example.backend.user.entity.Permission;
import com.example.backend.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for filtering users with builder pattern support. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User filter criteria")
public class UserFilter {

  @Schema(description = "Filter by user ID from (inclusive)", example = "1")
  private Long idFrom;

  @Schema(description = "Filter by user ID to (inclusive)", example = "100")
  private Long idTo;

  @Schema(description = "Filter by username (contains)", example = "john")
  private String username;

  @Schema(description = "Filter by first name (contains)", example = "John")
  private String firstName;

  @Schema(description = "Filter by last name (contains)", example = "Doe")
  private String lastName;

  @Schema(description = "Filter by email (contains)", example = "john@example.com")
  private String email;

  @Schema(description = "Filter by roles", example = "[\"ADMIN\", \"USER\"]")
  private List<UserRole> roles;

  @Schema(description = "Filter by permissions", example = "[\"READ\", \"CREATE\"]")
  private List<Permission> permissions;

  @Schema(description = "Filter by active status", example = "true")
  private Boolean isActive;

  @Schema(description = "Created date from", example = "2024-01-01T00:00:00Z")
  private Instant createdAtFrom;

  @Schema(description = "Created date to", example = "2024-12-31T23:59:59Z")
  private Instant createdAtTo;

  @Schema(description = "Updated date from", example = "2024-01-01T00:00:00Z")
  private Instant updatedAtFrom;

  @Schema(description = "Updated date to", example = "2024-12-31T23:59:59Z")
  private Instant updatedAtTo;

  @Schema(description = "Last login date from", example = "2024-01-01T00:00:00Z")
  private Instant lastLoginDateFrom;

  @Schema(description = "Last login date to", example = "2024-12-31T23:59:59Z")
  private Instant lastLoginDateTo;

  @Schema(description = "Filter by created by user", example = "admin")
  private String createdBy;

  @Schema(description = "Filter by updated by user", example = "admin")
  private String updatedBy;

  @Schema(description = "Filter by deleted by user", example = "admin")
  private String deletedBy;

  @Schema(
      description = "Filter by deletion status (ACTIVE_ONLY, DELETED_ONLY, ALL)",
      example = "ACTIVE_ONLY",
      defaultValue = "ACTIVE_ONLY")
  @Builder.Default
  private DeletionStatus deletionStatus = DeletionStatus.ACTIVE_ONLY;
}
