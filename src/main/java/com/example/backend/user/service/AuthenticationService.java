package com.example.backend.user.service;

import com.example.backend.common.utils.TestUtils;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.security.JwtService;
import com.example.backend.user.dto.AuthenticationResponse;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.dto.RegisterRequest;
import com.example.backend.user.entity.User;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service for handling user authentication operations. */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  /**
   * Registers a new user.
   *
   * @param request the registration request
   * @return authentication response with JWT tokens
   */
  @Transactional
  public AuthenticationResponse register(RegisterRequest request) {
    log.info("Registering new user: {}", TestUtils.toJsonString(request));

    // Check if username already exists
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("Username already exists: " + request.getUsername());
    }

    // Check if email already exists
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Email already exists: " + request.getEmail());
    }

    // Create user entity
    User user = userMapper.toEntity(request);
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getUserRole());
    user.setIsActive(true);

    // Save user
    User savedUser = userRepository.save(user);
    log.info("User registered successfully with id: {}", savedUser.getId());

    // Generate tokens
    String accessToken = jwtService.generateToken(savedUser);
    String refreshToken = jwtService.generateRefreshToken(savedUser);

    return AuthenticationResponse.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .tokenType("Bearer")
        .expiresIn(86400000L) // 24 hours in milliseconds
        .build();
  }

  /**
   * Authenticates a user and generates JWT tokens.
   *
   * @param request the login request
   * @return authentication response with JWT tokens
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

      // Update last login date
      user.updateLastLoginDate();
      userRepository.save(user);

      // Generate tokens
      String accessToken = jwtService.generateToken(user);
      String refreshToken = jwtService.generateRefreshToken(user);

      log.info("User logged in successfully: {}", user.getUsername());

      return AuthenticationResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(86400000L)
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
        .build();
  }
}
