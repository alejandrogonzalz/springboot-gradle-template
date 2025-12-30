package com.example.backend.user.entity;

import com.example.backend.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * User entity representing a user in the system.
 *
 * <p>Implements {@link UserDetails} for Spring Security integration using pure Spring Security
 * patterns and authorities.
 *
 * <p>Extends {@link BaseEntity} for common fields like id, timestamps, and version.
 */
@Entity
@Table(
    name = "users",
    indexes = {
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

  @Size(max = 20, message = "Phone must not exceed 20 characters")
  @Column(name = "phone", length = 20)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  private UserRole role;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "last_login_date")
  private Instant lastLoginDate;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "deleted_by", length = 50)
  private String deletedBy;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "permission")
  @Builder.Default
  private Set<Permission> additionalPermissions = new HashSet<>();

  // ========== UserDetails Implementation (Pure Spring Security) ==========

  /**
   * Returns all granted authorities for this user.
   *
   * <p>This includes:
   *
   * <ul>
   *   <li>Role authority (e.g., "ROLE_ADMIN")
   *   <li>Role-based permission authorities (e.g., "PERM_READ", "PERM_CREATE")
   *   <li>Additional custom permissions granted to this specific user
   * </ul>
   *
   * <p>Spring Security uses these authorities for @PreAuthorize and other security checks.
   *
   * @return collection of granted authorities
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();

    // Add role and its default permissions
    authorities.addAll(role.getAuthorities());

    // Add any additional permissions granted specifically to this user
    authorities.addAll(
        additionalPermissions.stream().map(Permission::toAuthority).collect(Collectors.toSet()));

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

  // ========== Business Logic Methods ==========

  /**
   * Changes the user's password using BCrypt encoding.
   *
   * @param newPassword the new plain text password
   */
  public void changePassword(String newPassword) {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    this.passwordHash = encoder.encode(newPassword);
  }

  /**
   * Resets the user's password to a randomly generated secure password.
   *
   * @return the newly generated password (should be sent to user via secure channel)
   */
  public String resetPassword() {
    String generatedPassword = generateRandomPassword();
    changePassword(generatedPassword);
    return generatedPassword;
  }

  /** Updates the last login date to the current timestamp. */
  public void updateLastLoginDate() {
    this.lastLoginDate = Instant.now();
  }

  /**
   * Gets the user's full name (first name + last name).
   *
   * @return concatenated first and last name
   */
  public String getFullName() {
    return this.firstName + " " + this.lastName;
  }

  // ========== Private Helper Methods ==========

  /**
   * Generates a random secure password with uppercase, lowercase, digits, and special characters.
   *
   * @return randomly generated 12-character password
   */
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
   * Checks if this user is soft-deleted.
   *
   * @return true if user is deleted (deletedAt is not null)
   */
  public boolean isDeleted() {
    return this.isActive == false;
  }

  /**
   * Soft deletes this user by setting deletedAt timestamp and marking as inactive.
   *
   * @param deletedBy username of the user performing the deletion
   */
  public void softDelete(String deletedBy) {
    this.deletedAt = Instant.now();
    this.deletedBy = deletedBy;
    this.isActive = false; // Also mark as inactive
  }

  /**
   * Restores a soft-deleted user by clearing deletion fields. Note: isActive remains false and must
   * be explicitly set if needed.
   */
  public void restore() {
    this.deletedAt = null;
    this.deletedBy = null;
    this.isActive = true;
    // Don't automatically set isActive = true (admin decides)
  }
}
