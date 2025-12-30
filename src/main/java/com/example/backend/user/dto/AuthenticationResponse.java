package com.example.backend.user.dto;

import com.example.backend.user.entity.Permission;
import com.example.backend.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response containing ONLY user information. Tokens are sent as HTTP-only
 * cookies, not in response body.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with user data only (tokens in HTTP-only cookies)")
public class AuthenticationResponse {

  @Schema(description = "User ID", example = "1")
  private Long userId;

  @Schema(description = "Username", example = "johndoe")
  private String username;

  @Schema(description = "Email address", example = "john.doe@example.com")
  private String email;

  @Schema(description = "First name", example = "John")
  private String firstName;

  @Schema(description = "Last name", example = "Doe")
  private String lastName;

  @Schema(description = "Phone number", example = "+1234567890")
  private String phone;

  @Schema(description = "User role", example = "USER")
  private UserRole role;

  @Schema(description = "User permissions", example = "[\"READ\", \"CREATE\", \"UPDATE\"]")
  private Set<Permission> permissions;

  @Schema(description = "Whether the user is active", example = "true")
  @Builder.Default
  private Boolean isActive = true;
}
