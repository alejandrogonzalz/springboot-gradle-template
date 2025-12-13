package com.example.backend.user.service;

import com.example.backend.security.JwtService;
import com.example.backend.user.dto.AuthenticationResponse;
import com.example.backend.user.dto.AuthenticationResponse.UserInfo;
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
   * Authenticates a user and generates JWT tokens.
   *
   * @param request the login request
   * @return authentication response with JWT tokens and user info
   */
  @Transactional
  public AuthenticationResponse login(LoginRequest request) {
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

      String accessToken = jwtService.generateToken(user);
      String refreshToken = jwtService.generateRefreshToken(user);

      // Store refresh token in database
      RefreshToken refreshTokenEntity =
          RefreshToken.builder()
              .token(refreshToken)
              .user(user)
              .expiresAt(Instant.now().plusSeconds(604800)) // 7 days
              .build();
      refreshTokenRepository.save(refreshTokenEntity);

      log.debug("Refresh token stored in database for user: {}", user.getUsername());

      log.info("User logged in successfully: {}", user.getUsername());

      return AuthenticationResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(900000L) // 15 minutes
          .user(buildUserInfo(user))
          .build();

    } catch (AuthenticationException e) {
      log.warn("Authentication failed for username: {}", request.getUsername());
      throw new BadCredentialsException("Invalid username or password");
    }
  }

  /**
   * Refreshes the access token using a valid refresh token.
   *
   * @param refreshToken the refresh token
   * @return authentication response with new access token
   */
  public AuthenticationResponse refreshToken(String refreshToken) {
    log.debug("Refreshing access token");

    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new IllegalArgumentException("Refresh token is empty");
    }

    // Check if token exists in database
    RefreshToken storedToken =
        refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

    // Check if token is expired
    if (storedToken.isExpired()) {
      refreshTokenRepository.delete(storedToken);
      throw new IllegalArgumentException("Refresh token has expired");
    }

    String username = jwtService.extractUsername(refreshToken);
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    if (!jwtService.isTokenValid(refreshToken, user)) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    String newAccessToken = jwtService.generateToken(user);

    // Return same refresh token (no rotation)
    return AuthenticationResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(900000L) // 15 minutes
        .user(buildUserInfo(user))
        .build();
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
   * Builds UserInfo from User entity.
   *
   * @param user the user entity
   * @return UserInfo DTO
   */
  private UserInfo buildUserInfo(User user) {
    return UserInfo.builder()
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
