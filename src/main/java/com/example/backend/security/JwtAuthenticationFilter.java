package com.example.backend.security;

import com.example.backend.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter that intercepts requests and validates JWT tokens from HTTP-only
 * cookies.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    final String jwt = extractJwtFromCookie(request);
    final String username;

    if (jwt == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      username = jwtService.extractUsername(jwt);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
          UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

          if (jwtService.isTokenValid(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
          }
        } catch (UsernameNotFoundException e) {
          log.warn("User account validation failed: {}", e.getMessage());
          sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
          return;
        }
      }
    } catch (Exception e) {
      log.warn("JWT validation failed: {}", e.getMessage());
      sendErrorResponse(
          response,
          HttpServletResponse.SC_UNAUTHORIZED,
          "Invalid or expired token. Please login again.");
      return;
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Sends a JSON error response with ApiResponse format.
   *
   * @param response the HTTP response
   * @param status HTTP status code
   * @param message error message
   */
  private void sendErrorResponse(HttpServletResponse response, int status, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Void> apiResponse = ApiResponse.error(message);
    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    response.getWriter().flush();
  }

  /**
   * Extracts JWT token from accessToken HTTP-only cookie.
   *
   * @param request the HTTP request
   * @return JWT token or null if not found
   */
  private String extractJwtFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return null;
    }

    for (Cookie cookie : request.getCookies()) {
      if ("accessToken".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }

    return null;
  }
}
