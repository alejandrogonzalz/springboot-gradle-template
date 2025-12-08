package com.example.backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request containing credentials")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "johndoe")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "SecurePass123!")
    private String password;
}
