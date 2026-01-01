package com.example.backend.audit.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be audited.
 *
 * <p>Usage:
 *
 * <pre>
 * &#64;Auditable(operation = "CREATE_USER", entityType = "User")
 * public User createUser(CreateUserRequest request) { ... }
 * </pre>
 *
 * <p>The AOP aspect will automatically capture: - Username from SecurityContext - Timestamp -
 * Operation type - Entity type and ID - Request details (IP, URI, HTTP method) - Changes
 * (before/after state if applicable)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

  /**
   * Type of operation being performed (e.g., "CREATE_USER", "UPDATE_USER", "DELETE_USER")
   *
   * @return operation name
   */
  String operation();

  /**
   * Type of entity being affected (e.g., "User", "Product", "Order")
   *
   * @return entity type name
   */
  String entityType();

  /**
   * Optional human-readable description template. Can use SpEL expressions. Example: "Created user
   * #{result.username}"
   *
   * @return description template
   */
  String description() default "";

  /**
   * Whether to capture the full request/response data in the changes field
   *
   * @return true to capture data
   */
  boolean captureData() default true;
}
