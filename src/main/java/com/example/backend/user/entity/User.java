package com.example.backend.user.entity;

import com.example.backend.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing a user in the system.
 *
 * <p>Implements {@link UserDetails} for Spring Security integration.</p>
 * <p>Extends {@link BaseEntity} for common fields like id, timestamps, and version.</p>
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_active", columnList = "is_active")
})
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login_date")
    private Instant lastLoginDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_permissions",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    /**
     * Authenticates the user by comparing the provided password with the stored hash.
     *
     * @param password the plain text password to verify
     * @return true if the password matches, false otherwise
     */
    public boolean authenticate(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder.matches(password, this.passwordHash);
    }

    /**
     * Checks if the user has a specific permission.
     *
     * @param permission the permission to check
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(Permission permission) {
        return this.permissions.contains(permission) ||
               getRolePermissions().contains(permission);
    }

    /**
     * Checks if the user has a specific permission by action name.
     *
     * @param action the permission action name
     * @return true if the user has the permission, false otherwise
     */
    public boolean hasPermission(String action) {
        try {
            Permission permission = Permission.valueOf(action);
            return hasPermission(permission);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Changes the user's password.
     *
     * @param newPassword the new plain text password
     */
    public void changePassword(String newPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        this.passwordHash = encoder.encode(newPassword);
    }

    /**
     * Resets the user's password to a randomly generated one.
     *
     * @return the newly generated password (to be sent to user via email)
     */
    public String resetPassword() {
        String generatedPassword = generateRandomPassword();
        changePassword(generatedPassword);
        return generatedPassword;
    }

    /**
     * Gets permissions based on the user's role.
     *
     * @return set of permissions granted by the role
     */
    public Set<Permission> getRolePermissions() {
        Set<Permission> rolePermissions = new HashSet<>();

        switch (this.role) {
            case ADMIN:
                // Admin has all permissions
                rolePermissions.add(Permission.READ);
                rolePermissions.add(Permission.CREATE);
                rolePermissions.add(Permission.UPDATE);
                rolePermissions.add(Permission.DELETE);
                rolePermissions.add(Permission.ADMIN);
                rolePermissions.add(Permission.MANAGE_USERS);
                rolePermissions.add(Permission.VIEW_AUDIT_LOGS);
                break;
            case USER:
                // Regular user can read, create, and update
                rolePermissions.add(Permission.READ);
                rolePermissions.add(Permission.CREATE);
                rolePermissions.add(Permission.UPDATE);
                break;
            case GUEST:
                // Guest can only read
                rolePermissions.add(Permission.READ);
                break;
        }

        return rolePermissions;
    }

    /**
     * Gets all permissions (role-based + custom).
     *
     * @return combined set of all permissions
     */
    public Set<Permission> getAllPermissions() {
        Set<Permission> allPermissions = new HashSet<>(getRolePermissions());
        allPermissions.addAll(this.permissions);
        return allPermissions;
    }

    // ========== UserDetails Implementation ==========

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role as authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.role.name()));

        // Add all permissions as authorities
        getAllPermissions().forEach(permission ->
            authorities.add(new SimpleGrantedAuthority(permission.name()))
        );

        return authorities;
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isActive;
    }

    // ========== Helper Methods ==========

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

    /**
     * Gets the user's full name.
     *
     * @return concatenated first and last name
     */
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    /**
     * Updates the last login date to now.
     */
    public void updateLastLoginDate() {
        this.lastLoginDate = Instant.now();
    }
}
