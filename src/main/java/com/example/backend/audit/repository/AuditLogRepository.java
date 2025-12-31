package com.example.backend.audit.repository;

import com.example.backend.audit.entity.AuditLog;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/** Repository for AuditLog entity. */
@Repository
public interface AuditLogRepository
    extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

  /** Find audit logs by username */
  Page<AuditLog> findByUsername(String username, Pageable pageable);

  /** Find audit logs by entity type and ID */
  Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

  /** Find audit logs by operation */
  Page<AuditLog> findByOperation(String operation, Pageable pageable);

  /** Find audit logs by entity type */
  Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

  /** Find audit logs in a date range */
  List<AuditLog> findByCreatedAtBetween(Instant start, Instant end);

  /** Count audit logs by username */
  long countByUsername(String username);

  /** Count audit logs by entity type */
  long countByEntityType(String entityType);
}
