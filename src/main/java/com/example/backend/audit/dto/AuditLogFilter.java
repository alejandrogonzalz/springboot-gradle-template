package com.example.backend.audit.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit log filter criteria")
public class AuditLogFilter {

  private String username;
  private String operation;
  private String entityType;
  private Long entityId;
  private String ipAddress;
  private String requestUri;
  private String httpMethod;
  private Boolean success;
  private List<String> usernames;
  private List<String> operations;
  private List<String> entityTypes;

  @Schema(description = "Creation date from", example = "2024-01-01")
  private Instant createdAtFrom;

  @Schema(description = "Creation date to", example = "2024-12-31")
  private Instant createdAtTo;
}
