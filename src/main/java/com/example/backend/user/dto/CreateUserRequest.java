package com.example.backend.user.dto;

import com.example.backend.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for user registration requests. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request")
public class CreateUserRequest {

  @NotBlank(message = "Username is required")
  @Size(min = 5, max = 20, message = "Username must be between 3 and 50 characters")
  @Schema(description = "Username", example = "johndoe")
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 30, message = "Password must be at least 8 characters")
  @Schema(description = "Password", example = "SecurePass123!")
  private String password;

  @NotBlank(message = "First name is required")
  @Size(max = 100, message = "First name must not exceed 100 characters")
  @Schema(description = "First name", example = "John")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(max = 100, message = "Last name must not exceed 100 characters")
  @Schema(description = "Last name", example = "Doe")
  private String lastName;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Schema(description = "Email address", example = "john.doe@example.com")
  private String email;

  @NotBlank(message = "Phone is required")
  @Size(max = 20, message = "Phone must not exceed 20 characters")
  @Schema(description = "Phone number", example = "+1234567890")
  private String phone;

  @NotNull(message = "Role is required")
  @Schema(description = "User role", example = "USER")
  private UserRole userRole;
}
