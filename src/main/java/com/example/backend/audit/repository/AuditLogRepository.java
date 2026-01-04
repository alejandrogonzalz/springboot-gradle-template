package com.example.backend.audit.repository;

import com.example.backend.audit.entity.AuditLog;
import com.example.backend.common.dto.ChartPointProjection;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for AuditLog entity. */
@Repository
public interface AuditLogRepository
    extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

  /** Delete audit logs created before specified date */
  long deleteByCreatedAtBefore(Instant cutoffDate);

  /** Delete audit logs for a specific entity */
  long deleteByEntityTypeAndEntityId(String entityType, Long entityId);

  /** Delete audit logs for a specific user */
  long deleteByUsername(String username);

  /** Delete all audit logs (use with caution!) */
  @Override
  void deleteAll();

  @Query(
      value =
          "SELECT DATE_FORMAT(created_at, '%Y-%m-%d') as label, COUNT(*) as value "
              + "FROM audit_logs "
              + "WHERE created_at >= :since "
              + "GROUP BY label "
              + "ORDER BY label ASC",
      nativeQuery = true)
  List<ChartPointProjection> countLogsByDayNative(@Param("since") Instant since);

  @Query(
      value =
          "SELECT operation as label, COUNT(*) as value "
              + "FROM audit_logs "
              + "WHERE created_at >= :since "
              + "GROUP BY operation",
      nativeQuery = true)
  List<ChartPointProjection> countLogsByOperationNative(@Param("since") Instant since);

  @Query(
      value =
          "SELECT username as label, COUNT(*) as value "
              + "FROM audit_logs "
              + "WHERE created_at >= :since "
              + "GROUP BY username "
              + "ORDER BY value DESC "
              + "LIMIT :limit",
      nativeQuery = true)
  List<ChartPointProjection> findTopActiveUsersNative(
      @Param("since") Instant since, @Param("limit") int limit);

  @Query(
      value =
          "SELECT IF(success = 1, 'Success', 'Failure') as label, COUNT(*) as value "
              + "FROM audit_logs "
              + "WHERE created_at >= :since "
              + "GROUP BY success",
      nativeQuery = true)
  List<ChartPointProjection> countLogsByStatusNative(@Param("since") Instant since);
}
