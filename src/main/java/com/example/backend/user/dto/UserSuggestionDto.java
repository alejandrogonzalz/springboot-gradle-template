package com.example.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for user suggestions in comboboxes")
public class UserSuggestionDto {

  @Schema(description = "User ID", example = "1")
  private Long id;

  @Schema(description = "Username", example = "john.doe")
  private String username;

  @Schema(description = "User's first name", example = "John")
  private String firstName;

  @Schema(description = "User's last name", example = "Doe")
  private String lastName;

  @Schema(description = "User's email address", example = "john.doe@example.com")
  private String email;
}
