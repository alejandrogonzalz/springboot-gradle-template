package com.example.backend.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Utility class for date parsing and conversion operations. */
public class DateUtils {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  private static final ZoneId UTC = ZoneId.of("UTC");

  /**
   * Parses a date string in dd-MM-yyyy format and converts to Instant at start of day (00:00:00
   * UTC).
   *
   * @param dateStr date string in dd-MM-yyyy format (e.g., "25-12-2024")
   * @return Instant representing start of day in UTC, or null if dateStr is null/empty
   * @throws IllegalArgumentException if date format is invalid
   */
  public static Instant parseToStartOfDay(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) {
      return null;
    }

    try {
      LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
      ZonedDateTime startOfDay = localDate.atStartOfDay(UTC);
      return startOfDay.toInstant();
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date format. Expected dd-MM-yyyy, got: " + dateStr, e);
    }
  }

  /**
   * Parses a date string in dd-MM-yyyy format and converts to Instant at end of day (23:59:59.999
   * UTC).
   *
   * @param dateStr date string in dd-MM-yyyy format (e.g., "25-12-2024")
   * @return Instant representing end of day in UTC, or null if dateStr is null/empty
   * @throws IllegalArgumentException if date format is invalid
   */
  public static Instant parseToEndOfDay(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) {
      return null;
    }

    try {
      LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
      ZonedDateTime endOfDay = localDate.atTime(23, 59, 59, 999_999_999).atZone(UTC);
      return endOfDay.toInstant();
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date format. Expected dd-MM-yyyy, got: " + dateStr, e);
    }
  }

  /**
   * Formats an Instant to dd-MM-yyyy string in UTC timezone.
   *
   * @param instant the instant to format
   * @return formatted date string, or null if instant is null
   */
  public static String formatToDate(Instant instant) {
    if (instant == null) {
      return null;
    }
    return instant.atZone(UTC).toLocalDate().format(DATE_FORMATTER);
  }
}
