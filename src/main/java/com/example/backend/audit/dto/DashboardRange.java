package com.example.backend.audit.dto;

import java.time.Instant;
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
   * Calculates the start instant for this range.
   *
   * @return Instant representing the start of the range
   */
  public Instant getStartInstant() {
    return Instant.now().minus(amount, unit);
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
