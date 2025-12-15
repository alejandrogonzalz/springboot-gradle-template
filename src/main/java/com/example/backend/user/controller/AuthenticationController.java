package com.example.backend.user.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.security.JwtService;
import com.example.backend.user.dto.AuthenticationResponse;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.entity.User;
import com.example.backend.user.service.AuthenticationService;
import com.example.backend.user.service.AuthenticationService.AuthenticationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 *
 * <p>Handles login, token refresh, and logout. Both access and refresh tokens are sent as HTTP-only
 * cookies for maximum security.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication operations")
@Slf4j
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final JwtService jwtService;
  private final String TOKEN_PATH = "/";

  @PostMapping("/login")
  @Operation(
      summary = "Authenticate user",
      description =
          "Authenticates user credentials and returns user data. "
              + "Access and refresh tokens are set as HTTP-only cookies.")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
      @Valid @RequestBody LoginRequest request) {

    // Authenticate and get user + response + tokens (all generated in service)
    AuthenticationResult result = authenticationService.login(request);

    // Set both tokens as HTTP-only cookies
    ResponseCookie accessCookie = createAccessTokenCookie(result.accessToken());
    ResponseCookie refreshCookie = createRefreshTokenCookie(result.refreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.success(result.response(), "Login successful"));
  }

  @PostMapping("/refresh")
  @Operation(
      summary = "Refresh access token",
      description =
          "Generates a new access token using refresh token from HTTP-only cookie. "
              + "New tokens are set as HTTP-only cookies.")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
      @CookieValue(name = "refreshToken") String refreshToken) {

    log.debug("Refreshing token from HTTP-only cookie");

    // Refresh and get new access token (all generated in service)
    AuthenticationResult result = authenticationService.refreshToken(refreshToken);

    // Set new tokens as HTTP-only cookies
    ResponseCookie accessCookie = createAccessTokenCookie(result.accessToken());
    ResponseCookie refreshCookie = createRefreshTokenCookie(result.refreshToken());

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.success(result.response(), "Token refreshed successfully"));
  }

  @PostMapping("/logout")
  @Operation(
      summary = "Logout user",
      description =
          "Logs out the current user by deleting their refresh token from the database "
              + "and clearing both token cookies.")
  public ResponseEntity<ApiResponse<Void>> logout(
      @CookieValue(name = "refreshToken", required = false) String refreshToken) {

    authenticationService.logout(refreshToken);

    // Clear both cookies
    ResponseCookie clearAccessCookie =
        ResponseCookie.from("accessToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path(TOKEN_PATH)
            .maxAge(0)
            .build();

    ResponseCookie clearRefreshCookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path(TOKEN_PATH)
            .maxAge(0)
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, clearAccessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
        .body(ApiResponse.success(null, "Logged out successfully"));
  }

  @GetMapping("/me")
  @Operation(
      summary = "Get current user information",
      description =
          "Returns the authenticated user's information based on the access token from cookie")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> getCurrentUser(
      @AuthenticationPrincipal User user) {

    if (user == null) {
      throw new UnauthorizedException("User cannot be null.");
    }

    AuthenticationResponse response = authenticationService.getUserInfo(user);
    return ResponseEntity.ok(ApiResponse.success(response, "User information retrieved"));
  }

  /**
   * Creates a secure HTTP-only cookie for the access token.
   *
   * <p>Access token is sent with ALL API requests (path="/")
   *
   * @param accessToken the access token value
   * @return ResponseCookie configured with security best practices
   */
  private ResponseCookie createAccessTokenCookie(String accessToken) {
    return ResponseCookie.from("accessToken", accessToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path(TOKEN_PATH)
        .maxAge(Duration.ofMillis(jwtService.getJwtExpiration()))
        .build();
  }

  /**
   * Creates a secure HTTP-only cookie for the refresh token.
   *
   * <p>Refresh token is ONLY sent to /api/v1/auth/ endpoints for security
   *
   * @param refreshToken the refresh token value
   * @return ResponseCookie configured with security best practices
   */
  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path(TOKEN_PATH)
        .maxAge(Duration.ofMillis(jwtService.getRefreshExpiration()))
        .build();
  }
}
