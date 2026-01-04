package com.example.backend.audit.service;

import com.example.backend.audit.dto.AuditDashboardStatsDto;
import com.example.backend.audit.dto.AuditLogDto;
import com.example.backend.audit.dto.AuditLogFilter;
import com.example.backend.audit.dto.DashboardRange;
import com.example.backend.audit.entity.AuditLog;
import com.example.backend.audit.mapper.AuditLogMapper;
import com.example.backend.audit.repository.AuditLogRepository;
import com.example.backend.common.specification.SpecificationBuilder;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  public List<AuditLogDto> getAllAuditLogsUnpaginated(AuditLogFilter filter, Pageable pageable) {
    log.debug("Fetching ALL audit logs (unpaginated) with filters: {}", filter);
    Sort sort = pageable.getSort();
    Specification<AuditLog> spec = buildAuditLogSpecification(filter);
    return auditLogRepository.findAll(spec, sort).stream().map(auditLogMapper::toDto).toList();
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

  /**
   * Gets dashboard statistics for the specified time range.
   *
   * @param range time range for statistics (defaults to LAST_7_DAYS if null)
   * @return aggregated dashboard statistics
   */
  @Transactional(readOnly = true)
  public AuditDashboardStatsDto getDashboardStatistics(DashboardRange range) {
    // Default to LAST_7_DAYS if null
    if (range == null) {
      range = DashboardRange.LAST_7_DAYS;
    }

    Instant since = range.getStartInstant();

    log.debug("Fetching dashboard statistics for range {} (since {})", range, since);

    return AuditDashboardStatsDto.builder()
        .logsOverTime(
            auditLogMapper.toChartPointDtoList(auditLogRepository.countLogsByDayNative(since)))
        .logsByOperation(
            auditLogMapper.toChartPointDtoList(
                auditLogRepository.countLogsByOperationNative(since)))
        .logsByUser(
            auditLogMapper.toChartPointDtoList(
                auditLogRepository.findTopActiveUsersNative(since, 5)))
        .logsByStatus(
            auditLogMapper.toChartPointDtoList(auditLogRepository.countLogsByStatusNative(since)))
        .build();
  }
}
