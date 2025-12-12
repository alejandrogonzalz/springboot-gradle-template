package com.example.backend.user.dto;

import com.example.backend.user.entity.Permission;
import com.example.backend.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for authentication response containing JWT tokens and user information. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with tokens and user data")
public class AuthenticationResponse {

  @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String refreshToken;

  @Schema(description = "Token type", example = "Bearer")
  @Builder.Default
  private String tokenType = "Bearer";

  @Schema(description = "Token expiration time in milliseconds", example = "86400000")
  private Long expiresIn;

  @Schema(description = "Authenticated user information")
  private UserInfo user;

  /** Nested user information DTO. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "User information")
  public static class UserInfo {

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "User role", example = "USER")
    private UserRole role;

    @Schema(description = "User permissions", example = "[\"READ\", \"CREATE\", \"UPDATE\"]")
    private Set<Permission> permissions;

    @Schema(description = "Whether the user is active", example = "true")
    @Builder.Default
    private Boolean isActive = true;
  }
}
