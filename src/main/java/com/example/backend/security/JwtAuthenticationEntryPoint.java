package com.example.backend.security;

import com.example.backend.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Custom authentication entry point for handling JWT authentication failures.
 *
 * <p>This is invoked when a user tries to access a protected resource without proper
 * authentication. It returns a 401 UNAUTHORIZED response with a clear JSON message.
 *
 * <p>Uses Spring's configured ObjectMapper for consistent date/time serialization.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    log.warn(
        "Authentication failed: {} - Path: {}",
        authException.getMessage(),
        request.getRequestURI());

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    ApiResponse<?> apiResponse =
        ApiResponse.builder()
            .success(false)
            .message("Authentication required. Please provide a valid token.")
            .build();

    objectMapper.writeValue(response.getOutputStream(), apiResponse);
  }
}
