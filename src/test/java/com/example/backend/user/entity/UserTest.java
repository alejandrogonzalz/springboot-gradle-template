package com.example.backend.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@DisplayName("User Entity Unit Tests - Pure Spring Security")
class UserTest {

  private User user;
  private BCryptPasswordEncoder encoder;

  @BeforeEach
  void setUp() {
    encoder = new BCryptPasswordEncoder();
    String hashedPassword = encoder.encode("password123");
    user =
        User.builder()
            .id(1L)
            .username("testuser")
            .passwordHash(hashedPassword)
            .firstName("Test")
            .lastName("User")
            .email("test@example.com")
            .role(UserRole.USER)
            .isActive(true)
            .additionalPermissions(new HashSet<>())
            .build();
  }

  @Test
  @DisplayName("Get authorities - should return role and role-based permissions")
  void getAuthoritiesShouldReturnRoleAndPermissions() {
    Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) user.getAuthorities();

    assertThat(authorities).isNotEmpty();
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_READ"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_CREATE"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_UPDATE"));
  }

  @Test
  @DisplayName("Get authorities - ADMIN should have all permissions")
  void getAuthoritiesForAdminShouldHaveAllPermissions() {
    user.setRole(UserRole.ADMIN);

    Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) user.getAuthorities();

    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_READ"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_CREATE"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_UPDATE"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_DELETE"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_ADMIN"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_MANAGE_USERS"));
  }

  @Test
  @DisplayName("Get authorities - GUEST should have only read permission")
  void getAuthoritiesForGuestShouldHaveOnlyRead() {
    user.setRole(UserRole.GUEST);

    Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) user.getAuthorities();

    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("ROLE_GUEST"));
    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_READ"));
    assertThat(authorities).noneMatch(auth -> auth.getAuthority().equals("PERMISSION_CREATE"));
    assertThat(authorities).noneMatch(auth -> auth.getAuthority().equals("PERMISSION_DELETE"));
  }

  @Test
  @DisplayName("Get authorities - should include additional permissions")
  void getAuthoritiesShouldIncludeAdditionalPermissions() {
    user.getAdditionalPermissions().add(Permission.DELETE);

    Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) user.getAuthorities();

    assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("PERMISSION_DELETE"));
  }

  @Test
  @DisplayName("Change password - should update password hash")
  void changePasswordShouldUpdatePasswordHash() {
    String oldHash = user.getPasswordHash();
    user.changePassword("NewPassword456!");

    assertThat(user.getPasswordHash()).isNotEqualTo(oldHash);
    assertThat(encoder.matches("NewPassword456!", user.getPasswordHash())).isTrue();
  }

  @Test
  @DisplayName("Reset password - should generate new password")
  void resetPasswordShouldGenerateNewPassword() {
    String oldHash = user.getPasswordHash();
    String newPassword = user.resetPassword();

    assertThat(newPassword).isNotNull();
    assertThat(newPassword).hasSize(12);
    assertThat(user.getPasswordHash()).isNotEqualTo(oldHash);
    assertThat(encoder.matches(newPassword, user.getPasswordHash())).isTrue();
  }

  @Test
  @DisplayName("Get full name - should return concatenated name")
  void getFullNameShouldReturnConcatenatedName() {
    String fullName = user.getFullName();
    assertThat(fullName).isEqualTo("Test User");
  }

  @Test
  @DisplayName("Update last login date - should set current timestamp")
  void updateLastLoginDateShouldSetCurrentTimestamp() {
    assertThat(user.getLastLoginDate()).isNull();
    user.updateLastLoginDate();
    assertThat(user.getLastLoginDate()).isNotNull();
  }

  @Test
  @DisplayName("Is account non locked - should return true when active")
  void isAccountNonLockedWhenActiveShouldReturnTrue() {
    user.setIsActive(true);
    assertThat(user.isAccountNonLocked()).isTrue();
  }

  @Test
  @DisplayName("Is account non locked - should return false when inactive")
  void isAccountNonLockedWhenInactiveShouldReturnFalse() {
    user.setIsActive(false);
    assertThat(user.isAccountNonLocked()).isFalse();
  }

  @Test
  @DisplayName("Is enabled - should match isActive status")
  void isEnabledShouldMatchIsActive() {
    user.setIsActive(true);
    assertThat(user.isEnabled()).isTrue();

    user.setIsActive(false);
    assertThat(user.isEnabled()).isFalse();
  }

  @Test
  @DisplayName("Get password - should return password hash")
  void getPasswordShouldReturnPasswordHash() {
    assertThat(user.getPassword()).isEqualTo(user.getPasswordHash());
  }

  @Test
  @DisplayName("Get username - should return username")
  void getUsernameShouldReturnUsername() {
    assertThat(user.getUsername()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Is account non expired - should always return true")
  void isAccountNonExpiredShouldAlwaysReturnTrue() {
    assertThat(user.isAccountNonExpired()).isTrue();
  }

  @Test
  @DisplayName("Is credentials non expired - should always return true")
  void isCredentialsNonExpiredShouldAlwaysReturnTrue() {
    assertThat(user.isCredentialsNonExpired()).isTrue();
  }
}
