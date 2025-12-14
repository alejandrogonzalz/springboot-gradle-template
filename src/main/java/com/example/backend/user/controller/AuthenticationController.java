package com.example.backend.user.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.exception.UnauthorizedException;
import com.example.backend.security.JwtService;
import com.example.backend.user.dto.AuthenticationResponse;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.entity.User;
import com.example.backend.user.service.AuthenticationService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
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

  @PostMapping("/login")
  @Operation(
      summary = "Authenticate user",
      description =
          "Authenticates user credentials and returns user data. "
              + "Access and refresh tokens are set as HTTP-only cookies.")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
      @Valid @RequestBody LoginRequest request) {

    // Authenticate and get user + response
    var result = authenticationService.login(request);

    // Generate tokens
    Map<String, String> tokens = authenticationService.generateTokens(result.user());

    // Set both tokens as HTTP-only cookies
    ResponseCookie accessCookie = createAccessTokenCookie(tokens.get("accessToken"));
    ResponseCookie refreshCookie = createRefreshTokenCookie(tokens.get("refreshToken"));

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

    // Refresh and get user + response
    var result = authenticationService.refreshToken(refreshToken);

    // Generate new tokens
    Map<String, String> tokens = authenticationService.generateTokens(result.user());

    // Set new tokens as HTTP-only cookies
    ResponseCookie accessCookie = createAccessTokenCookie(tokens.get("accessToken"));
    ResponseCookie refreshCookie = createRefreshTokenCookie(tokens.get("refreshToken"));

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
            .path("/")
            .maxAge(0)
            .build();

    ResponseCookie clearRefreshCookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/api/v1/auth/")
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
      throw new UnauthorizedException("Not authenticated. Please log in to access this resource.");
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
        .path("/")
        .maxAge(Duration.ofMinutes(15))
        .build();
  }

  /**
   * Creates a secure HTTP-only cookie for the refresh token.
   *
   * <p>Refresh token is ONLY sent to /api/v1/auth/refresh endpoint for security
   *
   * @param refreshToken the refresh token value
   * @return ResponseCookie configured with security best practices
   */
  private ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)
        .secure(true)
        .sameSite("Lax")
        .path("/api/v1/auth/")
        .maxAge(Duration.ofDays(1))
        .build();
  }

  @GetMapping("/debug/token")
  @Operation(
      summary = "Debug token claims (Development only)",
      description = "Decodes and returns all claims from the provided token. Remove in production!")
  public ResponseEntity<ApiResponse<Claims>> debugToken(
      @RequestHeader("Authorization") String authHeader) {

    String token = authHeader.substring(7);

    log.info("Debugging token claims");
    Claims claims = jwtService.extractAllClaimsPublic(token);

    log.info("  Token claims:");
    log.info("   ├─ Subject (username): {}", claims.getSubject());
    log.info("   ├─ Issued At: {}", claims.getIssuedAt());
    log.info("   ├─ Expiration: {}", claims.getExpiration());
    log.info("   ├─ Role: {}", claims.get("role"));
    log.info("   ├─ User ID: {}", claims.get("userId"));
    log.info("   ├─ Email: {}", claims.get("email"));
    log.info("   └─ Permissions: {}", claims.get("permissions"));

    return ResponseEntity.ok(ApiResponse.success(claims, "Token decoded successfully"));
  }
}
