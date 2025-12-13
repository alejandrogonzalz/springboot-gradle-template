package com.example.backend.user.service;

import com.example.backend.security.JwtService;
import com.example.backend.user.dto.AuthenticationResponse;
import com.example.backend.user.dto.AuthenticationResponse.UserInfo;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
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

      String accessToken = jwtService.generateToken(user);
      String refreshToken = jwtService.generateRefreshToken(user);

      log.info("User logged in successfully: {}", user.getUsername());

      return AuthenticationResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(86400000L)
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

    String username = jwtService.extractUsername(refreshToken);
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    if (!jwtService.isTokenValid(refreshToken, user)) {
      throw new IllegalArgumentException("Invalid refresh token");
    }

    String newAccessToken = jwtService.generateToken(user);

    return AuthenticationResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(86400000L)
        .user(buildUserInfo(user))
        .build();
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
