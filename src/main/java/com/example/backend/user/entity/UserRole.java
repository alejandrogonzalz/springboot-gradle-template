package com.example.backend.user.entity;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * User role enumeration using Spring Security role naming convention.
 *
 * <p>Roles are hierarchical with predefined permissions:
 *
 * <ul>
 *   <li>ADMIN: Full system access with all permissions
 *   <li>USER: Standard user with read, create, update permissions
 *   <li>GUEST: Limited access with read-only permissions
 * </ul>
 *
 * <p>Uses "ROLE_" prefix as per Spring Security conventions.
 */
@Getter
public enum UserRole {
  ADMIN(
      Set.of(
          Permission.READ,
          Permission.CREATE,
          Permission.UPDATE,
          Permission.DELETE,
          Permission.ADMIN,
          Permission.MANAGE_USERS,
          Permission.VIEW_AUDIT_LOGS)),
  USER(Set.of(Permission.READ, Permission.CREATE, Permission.UPDATE)),
  GUEST(Set.of(Permission.READ));

  private final Set<Permission> permissions;

  UserRole(Set<Permission> permissions) {
    this.permissions = permissions;
  }

  /**
   * Gets the Spring Security role name with "ROLE_" prefix.
   *
   * @return role name (e.g., "ROLE_ADMIN")
   */
  public String getRoleName() {
    return "ROLE_" + this.name();
  }

  /**
   * Converts this role to a Spring Security GrantedAuthority.
   *
   * @return GrantedAuthority representation
   */
  public GrantedAuthority toAuthority() {
    return new SimpleGrantedAuthority(getRoleName());
  }

  /**
   * Gets all authorities (role + permissions) for this role.
   *
   * <p>This includes:
   *
   * <ul>
   *   <li>The role itself (e.g., "ROLE_ADMIN")
   *   <li>All permissions granted by the role (e.g., "PERM_READ", "PERM_CREATE")
   * </ul>
   *
   * @return set of all granted authorities
   */
  public Set<GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities =
        permissions.stream().map(Permission::toAuthority).collect(Collectors.toSet());
    authorities.add(toAuthority());
    return authorities;
  }
}
