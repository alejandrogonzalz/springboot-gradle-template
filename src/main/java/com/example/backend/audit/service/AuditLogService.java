package com.example.backend.audit.service;

import com.example.backend.audit.dto.AuditLogDto;
import com.example.backend.audit.dto.AuditLogFilter;
import com.example.backend.audit.entity.AuditLog;
import com.example.backend.audit.mapper.AuditLogMapper;
import com.example.backend.audit.repository.AuditLogRepository;
import com.example.backend.common.specification.SpecificationBuilder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for querying audit logs with pagination and filtering.
 *
 * <p>Uses {@link SpecificationBuilder} to build complex queries dynamically based on filter
 * criteria.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final AuditLogMapper auditLogMapper;

  /**
   * Gets all audit logs with optional filtering and pagination.
   *
   * @param filter audit log filter criteria
   * @param pageable pagination information
   * @return Page of AuditLogDto
   */
  public Page<AuditLogDto> getAllAuditLogs(AuditLogFilter filter, Pageable pageable) {
    log.debug("Fetching audit logs with filter: {}", filter);
    Specification<AuditLog> spec = buildAuditLogSpecification(filter);
    return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toDto);
  }

  /**
   * Gets all audit logs without pagination - returns ALL records matching filter. Use with caution
   * for large datasets.
   *
   * @param filter user filter criteria
   * @return List of all UserDto matching the filter
   */
  public List<AuditLogDto> getAllAuditLogsUnpaginated(AuditLogFilter filter) {
    log.debug("Fetching ALL audit logs (unpaginated) with filters: {}", filter);
    Specification<AuditLog> spec = buildAuditLogSpecification(filter);
    return auditLogRepository.findAll(spec).stream().map(auditLogMapper::toDto).toList();
  }

  /**
   * Builds a Specification for AuditLog filtering using the fluent builder pattern.
   *
   * <p>Demonstrates the power of {@link SpecificationBuilder} - zero boilerplate code!
   */
  private Specification<AuditLog> buildAuditLogSpecification(AuditLogFilter filter) {
    return SpecificationBuilder.<AuditLog>builder()
        .contains("username", filter.getUsername())
        .contains("operation", filter.getOperation())
        .contains("entityType", filter.getEntityType())
        .equals("entityId", filter.getEntityId())
        .contains("ipAddress", filter.getIpAddress())
        .contains("requestUri", filter.getRequestUri())
        .contains("httpMethod", filter.getHttpMethod())
        .equals("success", filter.getSuccess())
        .between("createdAt", filter.getCreatedAtFrom(), filter.getCreatedAtTo())
        .in("username", filter.getUsernames())
        .in("operation", filter.getOperations())
        .in("entityType", filter.getEntityTypes())
        .build();
  }
}
