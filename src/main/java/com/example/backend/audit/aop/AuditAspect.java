package com.example.backend.audit.aop;

import com.example.backend.audit.entity.AuditLog;
import com.example.backend.audit.repository.AuditLogRepository;
import com.example.backend.common.BaseEntity;
import com.example.backend.common.utils.TestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP Aspect that intercepts methods annotated with @Auditable and logs them to audit_logs table.
 *
 * <p>Automatically captures: - Username from SecurityContext - Timestamp - Operation type - Entity
 * type and ID - Request details (IP, URI, HTTP method) - Request/response data - Success/failure
 * status
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

  private final AuditLogRepository auditLogRepository;

  /**
   * Intercepts all methods annotated with @Auditable.
   *
   * @param joinPoint the join point
   * @param auditable the annotation
   * @return the result of the method execution
   * @throws Throwable if method execution fails
   */
  @Around("@annotation(auditable)")
  public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
    long startTime = System.currentTimeMillis();

    // Capture request context
    String username = getCurrentUsername();
    HttpServletRequest request = getCurrentRequest();
    String ipAddress = getClientIpAddress(request);
    String requestUri = request != null ? request.getRequestURI() : null;
    String httpMethod = request != null ? request.getMethod() : null;

    // Capture method arguments (for before state)
    Object[] args = joinPoint.getArgs();
    String requestData = auditable.captureData() ? TestUtils.toJsonString(args) : null;

    Object result = null;
    boolean success = true;
    String errorMessage = null;

    try {
      // Execute the actual method
      result = joinPoint.proceed();
      return result;

    } catch (Exception e) {
      success = false;
      errorMessage = e.getMessage();
      log.error("Audited method failed: {}", e.getMessage(), e);
      throw e; // Re-throw to maintain normal exception flow

    } finally {
      // Extract entity ID from result (if it's a BaseEntity)
      Long entityId = extractEntityId(result);

      // Build changes JSON (before/after)
      String changes = buildChangesJson(requestData, result, auditable.captureData());

      // Build description
      String description =
          auditable.description().isEmpty()
              ? buildDefaultDescription(auditable.operation(), entityId)
              : auditable.description();

      // Calculate execution time
      long executionTime = System.currentTimeMillis() - startTime;

      // Create audit log entry
      AuditLog auditLog =
          AuditLog.builder()
              .username(username)
              .operation(auditable.operation())
              .entityType(auditable.entityType())
              .entityId(entityId)
              .description(description)
              .ipAddress(ipAddress)
              .requestUri(requestUri)
              .httpMethod(httpMethod)
              .changes(changes)
              .metadata("{\"executionTimeMs\": " + executionTime + "}")
              .success(success)
              .errorMessage(errorMessage)
              .build();

      // Save asynchronously to avoid blocking the main request
      saveAuditLogAsync(auditLog);

      log.debug(
          "Audit logged: {} - {} - {} - {}ms",
          auditable.operation(),
          auditable.entityType(),
          entityId,
          executionTime);
    }
  }

  /** Get current authenticated username */
  private String getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      return authentication.getName();
    }
    return "anonymous";
  }

  /** Get current HTTP request */
  private HttpServletRequest getCurrentRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attributes != null ? attributes.getRequest() : null;
  }

  /** Extract client IP address from request */
  private String getClientIpAddress(HttpServletRequest request) {
    if (request == null) return null;

    String[] headers = {
      "X-Forwarded-For",
      "Proxy-Client-IP",
      "WL-Proxy-Client-IP",
      "HTTP_X_FORWARDED_FOR",
      "HTTP_X_FORWARDED",
      "HTTP_X_CLUSTER_CLIENT_IP",
      "HTTP_CLIENT_IP",
      "HTTP_FORWARDED_FOR",
      "HTTP_FORWARDED",
      "HTTP_VIA",
      "REMOTE_ADDR"
    };

    for (String header : headers) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        // X-Forwarded-For can contain multiple IPs, take the first one
        return ip.split(",")[0].trim();
      }
    }

    return request.getRemoteAddr();
  }

  /** Extract entity ID from result object */
  private Long extractEntityId(Object result) {
    if (result == null) return null;

    // If result is a BaseEntity, get its ID
    if (result instanceof BaseEntity) {
      return ((BaseEntity) result).getId();
    }

    // If result has a getId() method, try to call it
    try {
      var method = result.getClass().getMethod("getId");
      Object id = method.invoke(result);
      if (id instanceof Long) {
        return (Long) id;
      }
    } catch (Exception e) {
      // Ignore, entity ID will be null
    }

    return null;
  }

  /** Build changes JSON with before/after state */
  private String buildChangesJson(String requestData, Object result, boolean captureData) {
    if (!captureData) return null;

    StringBuilder json = new StringBuilder("{");

    if (requestData != null) {
      json.append("\"request\": ").append(requestData);
    }

    if (result != null) {
      if (requestData != null) json.append(", ");
      json.append("\"response\": ").append(TestUtils.toJsonString(result));
    }

    json.append("}");
    return json.toString();
  }

  /** Build default description */
  private String buildDefaultDescription(String operation, Long entityId) {
    return operation + (entityId != null ? " (ID: " + entityId + ")" : "");
  }

  /** Save audit log asynchronously to avoid blocking the main thread */
  @Async
  public void saveAuditLogAsync(AuditLog auditLog) {
    try {
      auditLogRepository.save(auditLog);
    } catch (Exception e) {
      log.error("Failed to save audit log: {}", e.getMessage(), e);
      // Don't throw - audit failures shouldn't break the application
    }
  }
}
