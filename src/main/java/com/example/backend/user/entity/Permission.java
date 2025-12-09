package com.example.backend.user.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Global permission enumeration using Spring Security authority naming convention.
 *
 * <p>Each permission is prefixed with "PERM_" to distinguish from roles in Spring Security. This
 * follows Spring Security best practices for fine-grained access control.
 */
@Getter
public enum Permission {
  READ("PERMISSION_READ"),
  CREATE("PERMISSION_CREATE"),
  UPDATE("PERMISSION_UPDATE"),
  DELETE("PERMISSION_DELETE"),
  ADMIN("PERMISSION_ADMIN"),
  MANAGE_USERS("PERMISSION_MANAGE_USERS"),
  VIEW_AUDIT_LOGS("PERMISSION_VIEW_AUDIT_LOGS");

  private final String authority;

  Permission(String authority) {
    this.authority = authority;
  }

  /**
   * Converts this permission to a Spring Security GrantedAuthority.
   *
   * @return GrantedAuthority representation
   */
  public GrantedAuthority toAuthority() {
    return new SimpleGrantedAuthority(authority);
  }
}
