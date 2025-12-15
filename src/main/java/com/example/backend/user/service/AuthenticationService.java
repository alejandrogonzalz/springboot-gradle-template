package com.example.backend.user.service;

import com.example.backend.exception.UnauthorizedException;
import com.example.backend.security.JwtService;
import com.example.backend.user.dto.AuthenticationResponse;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.entity.RefreshToken;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.RefreshTokenRepository;
import com.example.backend.user.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling user authentication operations. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  /**
   * Authenticates a user and generates JWT tokens (returned separately as cookies).
   *
   * <p>This method returns ONLY user data in AuthenticationResponse. Tokens (access and refresh)
   * should be set as HTTP-only cookies by the controller.
   *
   * @param request login credentials
   * @return AuthenticationResult containing User entity, response DTO, and generated tokens
   * @throws BadCredentialsException if credentials are invalid
   */
  @Transactional
  public AuthenticationResult login(LoginRequest request) {
    log.info("User login attempt for username: {}", request.getUsername());

    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  request.getUsername(), request.getPassword()));

      User user = (User) authentication.getPrincipal();

      user.updateLastLoginDate();
      userRepository.save(user);

      // 🔒 SINGLE SESSION: Delete all existing refresh tokens for this user
      // This ensures user can only be logged in on ONE device at a time
      refreshTokenRepository.deleteByUser(user);

      // Generate both tokens
      String accessToken = jwtService.generateToken(user);
      String refreshToken = jwtService.generateRefreshToken(user);

      // Save refresh token to database
      RefreshToken refreshTokenEntity =
          RefreshToken.builder()
              .token(refreshToken)
              .user(user)
              .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshExpiration() / 1000))
              .build();
      refreshTokenRepository.save(refreshTokenEntity);

      log.info("User logged in successfully: {}", user.getUsername());

      // Return user, response, and tokens
      return new AuthenticationResult(
          user, buildAuthenticationResponse(user), accessToken, refreshToken);

    } catch (AuthenticationException e) {
      log.warn("Authentication failed for username: {}", request.getUsername());
      throw new BadCredentialsException("Invalid username or password");
    }
  }

  /**
   * Result containing User entity, AuthenticationResponse, and generated tokens for setting as
   * cookies.
   */
  public record AuthenticationResult(
      User user, AuthenticationResponse response, String accessToken, String refreshToken) {}

  /**
   * Refreshes the access token using a valid refresh token (returned separately as cookies).
   *
   * <p>This method generates a NEW access token and reuses the same refresh token (no rotation).
   *
   * @param refreshToken the refresh token
   * @return AuthenticationResult containing User entity, response DTO, and NEW tokens
   * @throws UnauthorizedException if refresh token is invalid or expired
   */
  @Transactional
  public AuthenticationResult refreshToken(String refreshToken) {
    log.debug("Refreshing access token");

    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new UnauthorizedException("Refresh token is empty");
    }

    // Check if token exists in database
    RefreshToken storedToken =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

    // Check if token is expired
    if (storedToken.isExpired()) {
      refreshTokenRepository.delete(storedToken);
      throw new UnauthorizedException("Refresh token has expired");
    }

    User user = storedToken.getUser();

    // Generate new access token (refresh token stays the same)
    String newAccessToken = jwtService.generateToken(user);

    log.debug("Token refreshed successfully for user: {}", user.getUsername());
    log.debug("   ├─ New access token generated");
    log.debug("   └─ Refresh token reused (not rotated)");

    // Return user, response, and tokens
    return new AuthenticationResult(
        user, buildAuthenticationResponse(user), newAccessToken, refreshToken);
  }

  /**
   * Logs out a user by deleting their refresh token from the database.
   *
   * @param refreshToken the refresh token to invalidate
   */
  @Transactional
  public void logout(String refreshToken) {
    if (refreshToken != null && !refreshToken.isEmpty()) {
      refreshTokenRepository.deleteByToken(refreshToken);
      log.info("User logged out successfully - refresh token deleted");
    } else {
      log.debug("No refresh token provided for logout");
    }
  }

  /**
   * Gets user information for the currently authenticated user.
   *
   * @param user the authenticated user
   * @return authentication response with user data
   */
  public AuthenticationResponse getUserInfo(User user) {
    log.debug("Getting user info for: {}", user.getUsername());
    return buildAuthenticationResponse(user);
  }

  /**
   * Builds authentication response with user information only (no tokens).
   *
   * @param user the authenticated user
   * @return authentication response with ONLY user data (no tokens)
   */
  private AuthenticationResponse buildAuthenticationResponse(User user) {
    return AuthenticationResponse.builder()
        .userId(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .role(user.getRole())
        .permissions(user.getAdditionalPermissions())
        .isActive(user.getIsActive())
        .build();
  }
}
