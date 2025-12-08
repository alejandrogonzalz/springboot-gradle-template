package com.example.backend.user.entity;

/**
 * Global permission enumeration.
 *
 * <p>Simple, global permissions that apply across all resources.
 */
public enum Permission {
  // Basic CRUD permissions
  READ,
  CREATE,
  UPDATE,
  DELETE,

  // Administrative permissions
  ADMIN,
  MANAGE_USERS,
  VIEW_AUDIT_LOGS
}
