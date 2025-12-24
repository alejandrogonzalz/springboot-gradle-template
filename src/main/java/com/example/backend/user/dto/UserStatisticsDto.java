package com.example.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for user table statistics. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User table statistics")
public class UserStatisticsDto {

  @Schema(description = "Total number of users (all statuses)", example = "150")
  private Long totalUsers;

  @Schema(description = "Total number of active users (isActive=true)", example = "120")
  private Long totalActiveUsers;

  @Schema(description = "Total number of inactive users (isActive=false)", example = "30")
  private Long totalInactiveUsers;

  @Schema(
      description = "Count of users per role (all statuses)",
      example = "{\"ADMIN\": 5, \"USER\": 140, \"GUEST\": 5}")
  private Map<String, Long> usersByRole;

  @Schema(
      description = "Count of users per status",
      example = "{\"ACTIVE\": 120, \"INACTIVE\": 30}")
  private Map<String, Long> usersByStatus;
}
