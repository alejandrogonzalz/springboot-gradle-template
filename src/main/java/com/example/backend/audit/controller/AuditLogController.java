package com.example.backend.audit.controller;

import com.example.backend.audit.dto.AuditLogDto;
import com.example.backend.audit.dto.AuditLogFilter;
import com.example.backend.audit.dto.AuditLogFilterRequest;
import com.example.backend.audit.mapper.AuditLogMapper;
import com.example.backend.audit.service.AuditLogService;
import com.example.backend.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  private final AuditLogMapper auditLogMapper;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_AUDIT_LOGS')")
  @Operation(
      summary = "Get all audit logs (GET)",
      description = "Retrieves audit logs via query parameters.")
  public ResponseEntity<BaseResponse<Page<AuditLogDto>>> getAllAuditLogs(
      @ParameterObject AuditLogFilterRequest request,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {

    log.debug("GET /api/v1/audit-logs - Request: {}", request);
    AuditLogFilter filter = auditLogMapper.toFilter(request);
    Page<AuditLogDto> auditLogs = auditLogService.getAllAuditLogs(filter, pageable);
    return ResponseEntity.ok(BaseResponse.success(auditLogs, "Audit logs retrieved successfully"));
  }

  @PostMapping("/all")
  @PreAuthorize("hasRole('ADMIN') or hasAuthority('VIEW_AUDIT_LOGS')")
  @Operation(
      summary = "Search audit logs (POST)",
      description =
          "Filter audit logs using a JSON request body. Useful for complex filters or exporting.")
  public ResponseEntity<BaseResponse<Page<AuditLogDto>>> searchAuditLogs(
      @RequestBody AuditLogFilterRequest request,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {

    log.debug("POST /api/v1/audit-logs/all - Request: {}", request);
    AuditLogFilter filter = auditLogMapper.toFilter(request);
    Page<AuditLogDto> auditLogs = auditLogService.getAllAuditLogs(filter, pageable);
    return ResponseEntity.ok(BaseResponse.success(auditLogs, "Audit logs retrieved successfully"));
  }
}
