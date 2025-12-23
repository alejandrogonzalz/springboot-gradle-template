package com.example.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.backend.security.JwtService;
import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.dto.LoginRequest;
import com.example.backend.user.entity.RefreshToken;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.repository.RefreshTokenRepository;
import com.example.backend.user.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Unit Tests")
class AuthenticationServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private UserMapper userMapper;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private JwtService jwtService;

  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthenticationService authenticationService;

  private CreateUserRequest createUserRequest;
  private LoginRequest loginRequest;
  private User testUser;

  @BeforeEach
  void setUp() {
    createUserRequest =
        CreateUserRequest.builder()
            .username("newuser")
            .password("SecurePass123!")
            .firstName("New")
            .lastName("User")
            .email("new@example.com")
            .build();

    loginRequest = LoginRequest.builder().username("testuser").password("password123").build();

    testUser =
        User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .passwordHash("hashedPassword")
            .role(UserRole.USER)
            .build();
  }

  @Test
  @DisplayName("Login - should authenticate and return tokens")
  void loginWithValidCredentialsShouldReturnAuthenticationResponse() {
    // Given
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUser);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token");
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(null);

    // When
    AuthenticationService.AuthenticationResult result = authenticationService.login(loginRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.user()).isNotNull();
    assertThat(result.user().getUsername()).isEqualTo("testuser");
    assertThat(result.response()).isNotNull();
    assertThat(result.response().getUsername()).isEqualTo("testuser");
    assertThat(result.response().getEmail()).isEqualTo("test@example.com");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository).save(testUser);
    verify(jwtService).generateRefreshToken(testUser);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("Login - should store refresh token in database")
  void loginShouldStoreRefreshTokenInDatabase() {
    // Given
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUser);
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(authentication);
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(jwtService.generateRefreshToken(testUser)).thenReturn("refresh-token-123");
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(null);

    // When
    authenticationService.login(loginRequest);

    // Then
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("Refresh token - should return new access token")
  void refreshTokenWithValidTokenShouldReturnNewAccessToken() {
    // Given
    String validRefreshToken = "valid-refresh-token";
    RefreshToken storedToken =
        RefreshToken.builder()
            .token(validRefreshToken)
            .user(testUser)
            .expiresAt(Instant.now().plusSeconds(604800))
            .build();

    when(refreshTokenRepository.findByToken(validRefreshToken))
        .thenReturn(Optional.of(storedToken));

    // When
    AuthenticationService.AuthenticationResult result =
        authenticationService.refreshToken(validRefreshToken);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.user()).isNotNull();
    assertThat(result.user().getUsername()).isEqualTo("testuser");
    assertThat(result.response().getUsername()).isEqualTo("testuser");
    verify(refreshTokenRepository).findByToken(validRefreshToken);
  }

  @Test
  @DisplayName("Logout - should delete refresh token from database")
  void logoutShouldDeleteRefreshTokenFromDatabase() {
    // Given
    String refreshToken = "refresh-token-to-delete";
    doNothing().when(refreshTokenRepository).deleteByToken(refreshToken);

    // When
    authenticationService.logout(refreshToken);

    // Then
    verify(refreshTokenRepository).deleteByToken(refreshToken);
  }
}
