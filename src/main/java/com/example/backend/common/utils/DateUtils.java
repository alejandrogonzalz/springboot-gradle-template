package com.example.backend.common.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Utility class for date parsing and conversion operations. */
public class DateUtils {

  // Legacy format (dd-MM-yyyy) - kept for backward compatibility
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

  // ISO-8601 formats (recommended for APIs)
  private static final DateTimeFormatter ISO_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter ISO_DATETIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  private static final ZoneId UTC = ZoneId.of("UTC");

  /**
   * @deprecated Use {@link #parseFlexibleDate(String)} instead for ISO-8601 support
   *     <p>Parses a date string in dd-MM-yyyy format and converts to Instant at start of day
   *     (00:00:00 UTC).
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
   * @deprecated Use {@link #parseFlexibleDateEndOfDay(String)} instead for ISO-8601 support
   *     <p>Parses a date string in dd-MM-yyyy format and converts to Instant at end of day
   *     (23:59:59.999 UTC).
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
   * Parses a flexible date/time string to Instant at start of day.
   *
   * <p>Supports multiple formats (auto-detected):
   *
   * <ul>
   *   <li>ISO-8601 date: "2024-01-01" → 2024-01-01T00:00:00Z
   *   <li>ISO-8601 datetime: "2024-01-01T10:30:00" → 2024-01-01T10:30:00Z
   *   <li>ISO-8601 instant: "2024-01-01T10:30:00Z" → 2024-01-01T10:30:00Z
   *   <li>Legacy format: "01-01-2024" → 2024-01-01T00:00:00Z (dd-MM-yyyy)
   * </ul>
   *
   * <p>All times without timezone are interpreted as UTC.
   *
   * @param dateTimeString the date or datetime string
   * @return Instant, or null if string is null/blank
   * @throws IllegalArgumentException if format is invalid
   */
  public static Instant parseFlexibleDate(String dateTimeString) {
    if (dateTimeString == null || dateTimeString.isBlank()) {
      return null;
    }

    String trimmed = dateTimeString.trim();

    try {
      // Try ISO-8601 instant with Z (e.g., "2024-01-01T10:30:00Z")
      return Instant.parse(trimmed);
    } catch (DateTimeParseException e) {
      // Not a full ISO instant
    }

    try {
      // Try ISO datetime (e.g., "2024-01-01T10:30:00")
      LocalDateTime dateTime = LocalDateTime.parse(trimmed, ISO_DATETIME_FORMATTER);
      return dateTime.atZone(UTC).toInstant();
    } catch (DateTimeParseException e) {
      // Not ISO datetime
    }

    try {
      // Try ISO date only (e.g., "2024-01-01")
      LocalDate date = LocalDate.parse(trimmed, ISO_DATE_FORMATTER);
      return date.atStartOfDay(UTC).toInstant();
    } catch (DateTimeParseException e) {
      // Not ISO date
    }

    try {
      // Fall back to legacy format (dd-MM-yyyy)
      return parseToStartOfDay(trimmed);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "Invalid date/time format: '"
              + dateTimeString
              + "'. Expected: yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss, yyyy-MM-ddTHH:mm:ssZ, or dd-MM-yyyy");
    }
  }

  /**
   * Parses a flexible date/time string to Instant at end of day.
   *
   * <p>Same as {@link #parseFlexibleDate} but sets time to 23:59:59.999999999 for date-only inputs.
   *
   * <p>Useful for "to" date filters to include the entire day.
   *
   * @param dateTimeString the date or datetime string
   * @return Instant at end of day if date only, otherwise parsed time
   * @throws IllegalArgumentException if format is invalid
   */
  public static Instant parseFlexibleDateEndOfDay(String dateTimeString) {
    if (dateTimeString == null || dateTimeString.isBlank()) {
      return null;
    }

    String trimmed = dateTimeString.trim();

    // If it contains 'T', it has time component - parse normally
    if (trimmed.contains("T")) {
      return parseFlexibleDate(trimmed);
    }

    // Date only - set to end of day
    Instant startOfDay = parseFlexibleDate(trimmed);
    return startOfDay != null
        ? startOfDay.atZone(UTC).toLocalDate().atTime(23, 59, 59, 999999999).atZone(UTC).toInstant()
        : null;
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
