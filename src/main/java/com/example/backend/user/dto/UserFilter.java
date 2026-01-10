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

  private Long idFrom;
  private Long idTo;
  private List<String> username;
  private String firstName;
  private String lastName;
  private String email;
  private List<UserRole> roles;
  private List<Permission> permissions;
  private Boolean isActive;
  private String phone;
  private List<String> createdBy;
  private List<String> updatedBy;
  private List<String> deletedBy;
  private Instant deletedAtFrom;
  private Instant deletedAtTo;
  private Instant createdAtFrom;
  private Instant createdAtTo;
  private Instant updatedAtFrom;
  private Instant updatedAtTo;
  private Instant lastLoginDateFrom;
  private Instant lastLoginDateTo;

  @Builder.Default private DeletionStatus deletionStatus = DeletionStatus.ACTIVE_ONLY;
}
