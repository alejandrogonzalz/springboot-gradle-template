package com.example.backend.audit.entity;

import com.example.backend.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Entity for tracking all auditable operations in the system.
 *
 * <p>Captures: - Who performed the action (username) - What action was performed (operation) -
 * Which entity was affected (entityType, entityId) - When it happened (timestamp from BaseEntity) -
 * Additional context (ipAddress, requestUri, changes)
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
      @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
      @Index(name = "idx_audit_user", columnList = "username"),
      @Index(name = "idx_audit_operation", columnList = "operation"),
      @Index(name = "idx_audit_created_at", columnList = "created_at")
    })
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {

  /** Username of the user who performed the action */
  @Column(name = "username", nullable = false, length = 50)
  private String username;

  /** Type of operation performed (CREATE, UPDATE, DELETE, etc.) */
  @Column(name = "operation", nullable = false, length = 50)
  private String operation;

  /** Type of entity affected (e.g., User, Product, Order) */
  @Column(name = "entity_type", nullable = false, length = 100)
  private String entityType;

  /** ID of the affected entity (nullable for operations without specific entity) */
  @Column(name = "entity_id")
  private Long entityId;

  /** Human-readable description of the action */
  @Column(name = "description", length = 500)
  private String description;

  /** IP address of the request */
  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  /** Request URI */
  @Column(name = "request_uri", length = 255)
  private String requestUri;

  /** HTTP method (POST, PUT, DELETE, etc.) */
  @Column(name = "http_method", length = 10)
  private String httpMethod;

  /** JSON representation of changes (before/after state) */
  @Column(name = "changes", columnDefinition = "TEXT")
  private String changes;

  /** Additional metadata (JSON format) */
  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata;

  /** Whether the operation was successful */
  @Column(name = "success", nullable = false)
  private Boolean success;

  /** Error message if operation failed */
  @Column(name = "error_message", length = 1000)
  private String errorMessage;
}
