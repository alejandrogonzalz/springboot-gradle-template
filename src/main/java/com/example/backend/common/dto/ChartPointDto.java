package com.example.backend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Generic data point for charting (X-axis Label vs Y-axis Value)")
public class ChartPointDto {

  @Schema(
      description = "The label for the chart axis (e.g., '2026-01-04', 'CREATE', 'user_1')",
      example = "CREATE")
  private String label;

  @Schema(description = "The magnitude or count for this label", example = "150")
  private Long value;
}
