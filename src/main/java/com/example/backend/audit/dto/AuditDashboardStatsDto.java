package com.example.backend.audit.dto;

import com.example.backend.common.dto.ChartPointDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Aggregated statistics for the Audit Log Dashboard")
public class AuditDashboardStatsDto {

  @Schema(description = "Daily volume of audit logs over the configured period")
  private List<ChartPointDto> logsOverTime;

  @Schema(description = "Breakdown of logs by operation type (CREATE, UPDATE, DELETE)")
  private List<ChartPointDto> logsByOperation;

  @Schema(description = "Top most active users by number of actions performed")
  private List<ChartPointDto> logsByUser;

  @Schema(description = "Ratio of successful vs failed operations")
  private List<ChartPointDto> logsByStatus;
}
