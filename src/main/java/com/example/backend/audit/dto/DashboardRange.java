package com.example.backend.audit.dto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/** Predefined time ranges for dashboard statistics. */
public enum DashboardRange {
  LAST_7_DAYS(7, ChronoUnit.DAYS),
  LAST_30_DAYS(30, ChronoUnit.DAYS),
  LAST_3_MONTHS(3, ChronoUnit.MONTHS),
  LAST_YEAR(1, ChronoUnit.YEARS);

  private final long amount;
  private final ChronoUnit unit;

  DashboardRange(long amount, ChronoUnit unit) {
    this.amount = amount;
    this.unit = unit;
  }

  /**
   * Calculates the start instant for this range. Uses ZonedDateTime to support calendar units like
   * Months and Years.
   */
  public Instant getStartInstant() {
    // Convert to ZonedDateTime (UTC) to perform calendar math safely
    return ZonedDateTime.now(ZoneOffset.UTC).minus(amount, unit).toInstant();
  }

  /**
   * Gets the amount of time units in this range.
   *
   * @return amount of units
   */
  public long getAmount() {
    return amount;
  }

  /**
   * Gets the time unit for this range.
   *
   * @return ChronoUnit (DAYS, MONTHS, YEARS)
   */
  public ChronoUnit getUnit() {
    return unit;
  }
}
