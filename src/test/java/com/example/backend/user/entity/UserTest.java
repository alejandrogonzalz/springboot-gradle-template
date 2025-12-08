package com.example.backend.user.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Entity Unit Tests")
class UserTest {

    private User user;
    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("password123");
        user = User.builder()
                .id(1L)
                .username("testuser")
                .passwordHash(hashedPassword)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .role(UserRole.USER)
                .isActive(true)
                .permissions(new HashSet<>())
                .build();
    }

    @Test
    @DisplayName("Authenticate - should return true with correct password")
    void authenticateWithCorrectPasswordShouldReturnTrue() {
        boolean result = user.authenticate("password123");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Authenticate - should return false with incorrect password")
    void authenticateWithIncorrectPasswordShouldReturnFalse() {
        boolean result = user.authenticate("wrongpassword");
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Has permission - should return true for role-based permission")
    void hasPermissionWithRoleBasedPermissionShouldReturnTrue() {
        boolean result = user.hasPermission(Permission.READ);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Has permission - should return false for missing permission")
    void hasPermissionWithMissingPermissionShouldReturnFalse() {
        boolean result = user.hasPermission(Permission.DELETE);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Change password - should update password hash")
    void changePasswordShouldUpdatePasswordHash() {
        String oldHash = user.getPasswordHash();
        user.changePassword("NewPassword456!");
        assertThat(user.getPasswordHash()).isNotEqualTo(oldHash);
        assertThat(user.authenticate("NewPassword456!")).isTrue();
    }

    @Test
    @DisplayName("Get role permissions - should return correct permissions for USER")
    void getRolePermissionsForUserRoleShouldReturnUserPermissions() {
        var permissions = user.getRolePermissions();
        assertThat(permissions).contains(Permission.READ, Permission.CREATE, Permission.UPDATE);
        assertThat(permissions).doesNotContain(Permission.DELETE, Permission.ADMIN);
    }

    @Test
    @DisplayName("Get full name - should return concatenated name")
    void getFullNameShouldReturnConcatenatedName() {
        String fullName = user.getFullName();
        assertThat(fullName).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Get authorities - should include role and permissions")
    void getAuthoritiesShouldIncludeRoleAndPermissions() {
        var authorities = user.getAuthorities();
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
    }
}
