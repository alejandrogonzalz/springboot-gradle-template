package com.example.backend.audit.controller;

import com.example.backend.audit.dto.AuditLogDto;
import com.example.backend.audit.dto.AuditLogFilter;
import com.example.backend.audit.service.AuditLogService;
import com.example.backend.common.BaseResponse;
import com.example.backend.common.utils.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for audit log management.
 *
 * <p>Only accessible to admins or users with VIEW_AUDIT_LOGS permission.
 *
 * <p>Provides comprehensive filtering and pagination capabilities for audit trail queries.
 */
@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Audit", description = "Audit log management (admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AuditLogController {

  private final AuditLogService auditLogService;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_AUDIT_LOGS')")
  @Operation(
      summary = "Get all audit logs",
      description =
          "Retrieves paginated audit logs with optional filters. "
              + "Default sort by createdAt descending (most recent first). "
              + "Requires ADMIN role or VIEW_AUDIT_LOGS permission.",
      responses = {
        @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
        @ApiResponse(
            responseCode = "403",
            description =
                "Forbidden - insufficient permissions (requires ADMIN or VIEW_AUDIT_LOGS)")
      })
  public ResponseEntity<BaseResponse<Page<AuditLogDto>>> getAllAuditLogs(
      @Parameter(description = "Filter by username (partial match)") @RequestParam(required = false)
          String username,
      @Parameter(description = "Filter by operation type (e.g., CREATE_USER, UPDATE_USER)")
          @RequestParam(required = false)
          String operation,
      @Parameter(description = "Filter by entity type (e.g., User, Product)")
          @RequestParam(required = false)
          String entityType,
      @Parameter(description = "Filter by entity ID") @RequestParam(required = false) Long entityId,
      @Parameter(description = "Filter by IP address") @RequestParam(required = false)
          String ipAddress,
      @Parameter(description = "Filter by request URI") @RequestParam(required = false)
          String requestUri,
      @Parameter(description = "Filter by HTTP method (GET, POST, PUT, DELETE)")
          @RequestParam(required = false)
          String httpMethod,
      @Parameter(description = "Filter by success status (true/false)")
          @RequestParam(required = false)
          Boolean success,
      @Parameter(
              description =
                  "Filter by creation date from (ISO-8601: yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss)",
              example = "2024-01-01")
          @RequestParam(required = false)
          String createdAtFrom,
      @Parameter(
              description =
                  "Filter by creation date to (ISO-8601: yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss)",
              example = "2024-12-31")
          @RequestParam(required = false)
          String createdAtTo,
      @Parameter(description = "Filter by multiple usernames") @RequestParam(required = false)
          List<String> usernames,
      @Parameter(description = "Filter by multiple operations") @RequestParam(required = false)
          List<String> operations,
      @Parameter(description = "Filter by multiple entity types") @RequestParam(required = false)
          List<String> entityTypes,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          @Parameter(hidden = true)
          Pageable pageable) {

    log.info(
        "GET /api/v1/audit-logs - username: {}, operation: {}, entityType: {}, entityId: {}",
        username,
        operation,
        entityType,
        entityId);

    // Parse date strings to Instant
    Instant createdAtFromInstant = DateUtils.parseFlexibleDate(createdAtFrom);
    Instant createdAtToInstant = DateUtils.parseFlexibleDateEndOfDay(createdAtTo);

    AuditLogFilter filter =
        AuditLogFilter.builder()
            .username(username)
            .operation(operation)
            .entityType(entityType)
            .entityId(entityId)
            .ipAddress(ipAddress)
            .requestUri(requestUri)
            .httpMethod(httpMethod)
            .success(success)
            .createdAtFrom(createdAtFromInstant)
            .createdAtTo(createdAtToInstant)
            .usernames(usernames)
            .operations(operations)
            .entityTypes(entityTypes)
            .build();

    Page<AuditLogDto> auditLogs = auditLogService.getAllAuditLogs(filter, pageable);

    log.info(
        "Returning {} audit logs (page {}/{})",
        auditLogs.getNumberOfElements(),
        auditLogs.getNumber() + 1,
        auditLogs.getTotalPages());

    return ResponseEntity.ok(BaseResponse.success(auditLogs, "Audit logs retrieved successfully"));
  }
}
