package com.example.backend.user.dto;

import com.example.backend.common.validation.ValidPhoneNumber;
import com.example.backend.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for user update requests (admin only). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User update request (admin only)")
public class UpdateUserRequest {

  @Size(max = 100, message = "First name must not exceed 100 characters")
  @Schema(description = "First name", example = "John")
  private String firstName;

  @Size(max = 100, message = "Last name must not exceed 100 characters")
  @Schema(description = "Last name", example = "Doe")
  private String lastName;

  @Email(message = "Email must be valid")
  @Schema(description = "Email address", example = "john.doe@example.com")
  private String email;

  @ValidPhoneNumber
  @Size(max = 20, message = "Phone number must not exceed 20 characters")
  @Schema(description = "Phone number", example = "+1234567890")
  private String phone;

  @Schema(description = "Account active status", example = "true")
  private Boolean isActive;

  @Schema(description = "User role", example = "USER")
  private UserRole role;
}
