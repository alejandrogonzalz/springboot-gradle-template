package com.example.backend.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

/**
 * Unit tests for User entity focusing on security-critical methods.
 *
 * <p>These tests ensure Spring Security integration works correctly.
 */
@DisplayName("User Entity - Security Tests")
class UserTest {

  private User adminUser;
  private User regularUser;
  private User guestUser;

  @BeforeEach
  void setUp() {
    adminUser =
        User.builder()
            .id(1L)
            .username("admin")
            .email("admin@example.com")
            .passwordHash("hashedPassword")
            .role(UserRole.ADMIN)
            .isActive(true)
            .build();

    regularUser =
        User.builder()
            .id(2L)
            .username("user")
            .email("user@example.com")
            .passwordHash("hashedPassword")
            .role(UserRole.USER)
            .isActive(true)
            .build();

    guestUser =
        User.builder()
            .id(3L)
            .username("guest")
            .email("guest@example.com")
            .passwordHash("hashedPassword")
            .role(UserRole.GUEST)
            .isActive(true)
            .build();
  }

  @Test
  @DisplayName("getAuthorities() - Admin should have all permissions")
  void getAuthorities_AdminUser_ShouldHaveAllPermissions() {
    // When
    var authorities = adminUser.getAuthorities();

    // Then
    assertThat(authorities).isNotEmpty();
    assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .contains(
            "ROLE_ADMIN",
            "PERMISSION_READ",
            "PERMISSION_CREATE",
            "PERMISSION_UPDATE",
            "PERMISSION_DELETE",
            "PERMISSION_ADMIN",
            "PERMISSION_MANAGE_USERS",
            "PERMISSION_VIEW_AUDIT_LOGS");
  }

  @Test
  @DisplayName("getAuthorities() - Regular user should have limited permissions")
  void getAuthorities_RegularUser_ShouldHaveLimitedPermissions() {
    // When
    var authorities = regularUser.getAuthorities();

    // Then
    assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_USER", "PERMISSION_READ", "PERMISSION_CREATE", "PERMISSION_UPDATE")
        .doesNotContain("PERMISSION_DELETE", "PERMISSION_ADMIN", "PERMISSION_MANAGE_USERS");
  }

  @Test
  @DisplayName("getAuthorities() - Guest should have only read permission")
  void getAuthorities_GuestUser_ShouldHaveOnlyReadPermission() {
    // When
    var authorities = guestUser.getAuthorities();

    // Then
    assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .contains("ROLE_GUEST", "PERMISSION_READ")
        .doesNotContain(
            "PERMISSION_CREATE", "PERMISSION_UPDATE", "PERMISSION_DELETE", "PERMISSION_ADMIN");
  }

  @Test
  @DisplayName("getAuthorities() - Should include additional permissions")
  void getAuthorities_WithAdditionalPermissions_ShouldIncludeThem() {
    // Given
    regularUser.setAdditionalPermissions(Set.of(Permission.DELETE, Permission.ADMIN));

    // When
    var authorities = regularUser.getAuthorities();

    // Then
    assertThat(authorities)
        .extracting(GrantedAuthority::getAuthority)
        .contains("PERMISSION_DELETE", "PERMISSION_ADMIN");
  }

  @Test
  @DisplayName("isAccountNonLocked() - Active user should not be locked")
  void isAccountNonLocked_WhenActive_ShouldReturnTrue() {
    // Given
    adminUser.setIsActive(true);

    // When & Then
    assertThat(adminUser.isAccountNonLocked()).isTrue();
  }

  @Test
  @DisplayName("isAccountNonLocked() - Inactive user should be locked")
  void isAccountNonLocked_WhenInactive_ShouldReturnFalse() {
    // Given
    adminUser.setIsActive(false);

    // When & Then
    assertThat(adminUser.isAccountNonLocked()).isFalse();
  }

  @Test
  @DisplayName("isEnabled() - Should match isActive flag")
  void isEnabled_ShouldMatchIsActiveFlag() {
    // When active
    adminUser.setIsActive(true);
    assertThat(adminUser.isEnabled()).isTrue();

    // When inactive
    adminUser.setIsActive(false);
    assertThat(adminUser.isEnabled()).isFalse();
  }

  @Test
  @DisplayName("getPassword() - Should return password hash")
  void getPassword_ShouldReturnPasswordHash() {
    // When & Then
    assertThat(adminUser.getPassword()).isEqualTo("hashedPassword");
  }

  @Test
  @DisplayName("getUsername() - Should return username for Spring Security")
  void getUsername_ShouldReturnUsernameForSpringSecurity() {
    // When & Then
    assertThat(adminUser.getUsername()).isEqualTo("admin");
  }

  @Test
  @DisplayName("isAccountNonExpired() - Should always return true")
  void isAccountNonExpired_ShouldAlwaysReturnTrue() {
    // When & Then
    assertThat(adminUser.isAccountNonExpired()).isTrue();
  }

  @Test
  @DisplayName("isCredentialsNonExpired() - Should always return true")
  void isCredentialsNonExpired_ShouldAlwaysReturnTrue() {
    // When & Then
    assertThat(adminUser.isCredentialsNonExpired()).isTrue();
  }
}
