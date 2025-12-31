package com.example.backend.audit.service;

import com.example.backend.audit.repository.AuditLogRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing audit log cleanup and maintenance.
 *
 * <p>Automatically deletes audit logs older than the configured retention period. Runs monthly by
 * default.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditCleanupService {

  private final AuditLogRepository auditLogRepository;

  /**
   * Retention period in days. Audit logs older than this will be deleted. Default: 90 days (3
   * months)
   */
  @Value("${audit.retention.days:90}")
  private int retentionDays;

  /** Whether automatic cleanup is enabled. Default: true */
  @Value("${audit.cleanup.enabled:true}")
  private boolean cleanupEnabled;

  /**
   * Scheduled cleanup job that runs on the 1st day of every month at 2:00 AM.
   *
   * <p>Cron format: "0 0 2 1 * ?" - Second Minute Hour DayOfMonth Month DayOfWeek
   *
   * <p>To disable: set audit.cleanup.enabled=false in application.properties
   */
  @Scheduled(cron = "${audit.cleanup.cron:0 0 2 1 * ?}")
  @Transactional
  public void cleanupOldAuditLogs() {
    if (!cleanupEnabled) {
      log.debug("Audit log cleanup is disabled");
      return;
    }

    log.info("Starting scheduled audit log cleanup (retention: {} days)", retentionDays);

    try {
      Instant cutoffDate = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
      long deletedCount = deleteAuditLogsBefore(cutoffDate);

      log.info(
          "Audit log cleanup completed successfully. Deleted {} records older than {}",
          deletedCount,
          cutoffDate);

    } catch (Exception e) {
      log.error("Failed to cleanup audit logs: {}", e.getMessage(), e);
      // Don't throw - cleanup failures shouldn't break the system
    }
  }

  /**
   * Deletes all audit logs created before the specified date.
   *
   * @param cutoffDate the cutoff date (logs before this will be deleted)
   * @return number of records deleted
   */
  @Transactional
  public long deleteAuditLogsBefore(Instant cutoffDate) {
    log.info("Deleting audit logs created before: {}", cutoffDate);

    long countBefore = auditLogRepository.count();
    auditLogRepository.deleteByCreatedAtBefore(cutoffDate);
    long countAfter = auditLogRepository.count();

    long deletedCount = countBefore - countAfter;
    log.info("Deleted {} audit log records", deletedCount);

    return deletedCount;
  }

  /**
   * Deletes all audit logs for a specific entity.
   *
   * @param entityType the entity type (e.g., "User")
   * @param entityId the entity ID
   * @return number of records deleted
   */
  @Transactional
  public long deleteAuditLogsForEntity(String entityType, Long entityId) {
    log.info("Deleting audit logs for entity: {} (ID: {})", entityType, entityId);

    long deletedCount = auditLogRepository.deleteByEntityTypeAndEntityId(entityType, entityId);

    log.info("Deleted {} audit log records for {} (ID: {})", deletedCount, entityType, entityId);
    return deletedCount;
  }

  /**
   * Deletes all audit logs for a specific user.
   *
   * @param username the username
   * @return number of records deleted
   */
  @Transactional
  public long deleteAuditLogsForUser(String username) {
    log.info("Deleting audit logs for user: {}", username);

    long deletedCount = auditLogRepository.deleteByUsername(username);

    log.info("Deleted {} audit log records for user: {}", deletedCount, username);
    return deletedCount;
  }

  /**
   * Gets the current retention period in days.
   *
   * @return retention period in days
   */
  public int getRetentionDays() {
    return retentionDays;
  }

  /**
   * Checks if automatic cleanup is enabled.
   *
   * @return true if enabled
   */
  public boolean isCleanupEnabled() {
    return cleanupEnabled;
  }
}
