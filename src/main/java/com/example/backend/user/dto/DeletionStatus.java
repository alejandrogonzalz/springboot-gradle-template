package com.example.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Enum for filtering users by deletion status. */
@Schema(description = "Deletion status filter for users")
public enum DeletionStatus {
  @Schema(description = "Show only active users (deletedAt IS NULL) - Default")
  ACTIVE_ONLY,

  @Schema(description = "Show only deleted users (deletedAt IS NOT NULL)")
  DELETED_ONLY,

  @Schema(description = "Show all users regardless of deletion status")
  ALL
}
