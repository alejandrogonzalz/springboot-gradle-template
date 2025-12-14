package com.example.backend.user.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.user.dto.AuthenticationResponse;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 *
 * <p>Handles login and token refresh. Registration is handled by UserController and requires admin
 * permissions.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication operations")
@Slf4j
public class AuthenticationController {

  private final AuthenticationService authenticationService;

  @PostMapping("/login")
  @Operation(
      summary = "Authenticate user",
      description =
          "Authenticates user credentials and returns JWT tokens. Refresh token is set as HTTP-only cookie.")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    AuthenticationResponse response = authenticationService.login(request);

    ResponseCookie refreshCookie = createRefreshTokenCookie(response.getRefreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.success(response, "Login successful"));
  }

  @PostMapping("/refresh")
  @Operation(
      summary = "Refresh access token",
      description = "Generates a new access token using refresh token from HTTP-only cookie")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
      @CookieValue(name = "refreshToken") String refreshToken) {

    log.debug("Refreshing token from HTTP-only cookie");

    AuthenticationResponse response = authenticationService.refreshToken(refreshToken);

    ResponseCookie refreshCookie = createRefreshTokenCookie(response.getRefreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.success(response, "Token refreshed successfully"));
  }

  @PostMapping("/logout")
  @Operation(
      summary = "Logout user",
      description =
          "Logs out the current user by deleting their refresh token from the database. "
              + "The access token will remain valid until it expires (15 minutes).")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken) {

    authenticationService.logout(refreshToken);

    // Clear the refresh token cookie
    // Clear the refresh token cookie with same attributes as when it was set
    ResponseCookie cookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(0)
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(ApiResponse.success(null, "Logged out successfully"));
  }

  /**
   * Creates a secure HTTP-only cookie for the refresh token.
   *
   * @param refreshToken the refresh token value
   * @return ResponseCookie configured with security best practices
   */
  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.ofDays(1))
        .build();
  }
}
