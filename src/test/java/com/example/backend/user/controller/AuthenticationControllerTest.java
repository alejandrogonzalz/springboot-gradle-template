package com.example.backend.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.backend.common.ApiResponse;
import com.example.backend.user.dto.AuthenticationResponse;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.dto.RegisterRequest;
import com.example.backend.user.entity.UserRole;
import com.example.backend.user.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Unit Tests - HTTP-Only Cookie")
class AuthenticationControllerTest {

  @Mock private AuthenticationService authenticationService;

  @InjectMocks private AuthenticationController authenticationController;

  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private AuthenticationResponse authResponse;

  @BeforeEach
  void setUp() {
    registerRequest =
        RegisterRequest.builder()
            .username("newuser")
            .password("SecurePass123!")
            .firstName("New")
            .lastName("User")
            .email("new@example.com")
            .userRole(UserRole.USER)
            .build();

    loginRequest = LoginRequest.builder().username("testuser").password("password123").build();

    authResponse =
        AuthenticationResponse.builder()
            .accessToken("access-token-value")
            .refreshToken("refresh-token-value")
            .tokenType("Bearer")
            .expiresIn(86400000L)
            .build();
  }

  @Test
  @DisplayName("Login - should return tokens and set HTTP-only refresh cookie")
  void loginShouldReturnTokensAndSetHttpOnlyCookie() {
    when(authenticationService.login(loginRequest)).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthenticationResponse>> response =
        authenticationController.login(loginRequest);

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getAccessToken()).isEqualTo("access-token-value");

    String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).isNotNull();
    assertThat(setCookieHeader).contains("refreshToken=refresh-token-value");
    assertThat(setCookieHeader).contains("HttpOnly");

    verify(authenticationService).login(loginRequest);
  }

  @Test
  @DisplayName("Refresh - should use refresh token from HTTP-only cookie")
  void refreshTokenShouldUseHttpOnlyCookie() {
    when(authenticationService.refreshToken("cookie-refresh-token")).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthenticationResponse>> response =
        authenticationController.refreshToken("cookie-refresh-token");

    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getAccessToken()).isEqualTo("access-token-value");

    String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).isNotNull();
    assertThat(setCookieHeader).contains("refreshToken=refresh-token-value");

    verify(authenticationService).refreshToken("cookie-refresh-token");
  }

  @Test
  @DisplayName("Refresh - should set new refresh cookie after refresh")
  void refreshTokenShouldSetNewRefreshCookie() {
    AuthenticationResponse newAuthResponse =
        AuthenticationResponse.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .tokenType("Bearer")
            .expiresIn(86400000L)
            .build();

    when(authenticationService.refreshToken("old-refresh-token")).thenReturn(newAuthResponse);

    ResponseEntity<ApiResponse<AuthenticationResponse>> response =
        authenticationController.refreshToken("old-refresh-token");

    String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).contains("refreshToken=new-refresh-token");
  }

  @Test
  @DisplayName("Cookie configuration - should have HttpOnly flag for XSS protection")
  void cookieShouldHaveHttpOnlyFlag() {
    when(authenticationService.login(loginRequest)).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthenticationResponse>> response =
        authenticationController.login(loginRequest);

    String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).contains("HttpOnly");
  }

  @Test
  @DisplayName("Cookie configuration - should have Secure flag for HTTPS only")
  void cookieShouldHaveSecureFlag() {
    when(authenticationService.login(loginRequest)).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthenticationResponse>> response =
        authenticationController.login(loginRequest);

    String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).contains("Secure");
  }

  @Test
  @DisplayName("Cookie configuration - should have SameSite=Lax for CSRF protection")
  void cookieShouldHaveSameSiteLax() {
    when(authenticationService.login(loginRequest)).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthenticationResponse>> response =
        authenticationController.login(loginRequest);

    String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).contains("SameSite=Lax");
  }

  @Test
  @DisplayName("Cookie configuration - should have correct path")
  void cookieShouldHaveCorrectPath() {
    when(authenticationService.login(loginRequest)).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthenticationResponse>> response =
        authenticationController.login(loginRequest);

    String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    assertThat(setCookieHeader).contains("Path=/api/v1/auth");
  }

  @Test
  @DisplayName("Cookie configuration - should have 1 day expiration")
  void cookieShouldHaveOneDayExpiration() {
    when(authenticationService.login(loginRequest)).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthenticationResponse>> response =
        authenticationController.login(loginRequest);

    String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    log.info("Set-Cookie Header: {}", setCookieHeader);
    assertThat(setCookieHeader).contains("Max-Age=86400");
  }
}
