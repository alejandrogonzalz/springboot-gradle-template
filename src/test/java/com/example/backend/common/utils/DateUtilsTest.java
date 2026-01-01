package com.example.backend.common.utils;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for DateUtils flexible date parsing. */
@DisplayName("DateUtils Tests")
class DateUtilsTest {

  @Nested
  @DisplayName("parseFlexibleDate() - Start of Day")
  class ParseFlexibleDateTests {

    @Test
    @DisplayName("Should parse ISO date to start of day")
    void shouldParseIsoDateToStartOfDay() {
      // Given
      String dateStr = "2024-01-15";

      // When
      Instant result = DateUtils.parseFlexibleDate(dateStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T00:00:00Z");
    }

    @Test
    @DisplayName("Should parse ISO datetime with time preserved")
    void shouldParseIsoDateTimeWithTimePreserved() {
      // Given
      String dateTimeStr = "2024-01-15T10:30:00";

      // When
      Instant result = DateUtils.parseFlexibleDate(dateTimeStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T10:30:00Z");
    }

    @Test
    @DisplayName("Should parse ISO instant with Z suffix")
    void shouldParseIsoInstantWithZSuffix() {
      // Given
      String instantStr = "2024-01-15T10:30:45Z";

      // When
      Instant result = DateUtils.parseFlexibleDate(instantStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T10:30:45Z");
    }

    @Test
    @DisplayName("Should parse legacy format dd-MM-yyyy")
    void shouldParseLegacyFormat() {
      // Given
      String legacyDateStr = "15-01-2024";

      // When
      Instant result = DateUtils.parseFlexibleDate(legacyDateStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T00:00:00Z");
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNullInput() {
      // When
      Instant result = DateUtils.parseFlexibleDate(null);

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for blank input")
    void shouldReturnNullForBlankInput() {
      // When
      Instant result = DateUtils.parseFlexibleDate("   ");

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should trim whitespace before parsing")
    void shouldTrimWhitespaceBeforeParsing() {
      // Given
      String dateStrWithSpaces = "  2024-01-15  ";

      // When
      Instant result = DateUtils.parseFlexibleDate(dateStrWithSpaces);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T00:00:00Z");
    }

    @Test
    @DisplayName("Should throw exception for invalid format")
    void shouldThrowExceptionForInvalidFormat() {
      // Given
      String invalidDateStr = "invalid-date";

      // When & Then
      assertThatThrownBy(() -> DateUtils.parseFlexibleDate(invalidDateStr))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid date/time format")
          .hasMessageContaining("invalid-date");
    }

    @Test
    @DisplayName("Should parse midnight correctly")
    void shouldParseMidnightCorrectly() {
      // Given
      String midnightStr = "2024-01-15T00:00:00";

      // When
      Instant result = DateUtils.parseFlexibleDate(midnightStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T00:00:00Z");
    }

    @Test
    @DisplayName("Should parse end of minute correctly")
    void shouldParseEndOfMinuteCorrectly() {
      // Given
      String timeStr = "2024-01-15T23:59:00";

      // When
      Instant result = DateUtils.parseFlexibleDate(timeStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T23:59:00Z");
    }
  }

  @Nested
  @DisplayName("parseFlexibleDateEndOfDay() - End of Day")
  class ParseFlexibleDateEndOfDayTests {

    @Test
    @DisplayName("Should parse ISO date to end of day")
    void shouldParseIsoDateToEndOfDay() {
      // Given
      String dateStr = "2024-01-15";

      // When
      Instant result = DateUtils.parseFlexibleDateEndOfDay(dateStr);

      // Then
      assertThat(result).isNotNull();
      String resultStr = result.atZone(ZoneId.of("UTC")).toString();
      assertThat(resultStr).startsWith("2024-01-15T23:59:59.999999999");
    }

    @Test
    @DisplayName("Should preserve time when datetime provided")
    void shouldPreserveTimeWhenDateTimeProvided() {
      // Given
      String dateTimeStr = "2024-01-15T10:30:00";

      // When
      Instant result = DateUtils.parseFlexibleDateEndOfDay(dateTimeStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T10:30:00Z");
    }

    @Test
    @DisplayName("Should preserve time when instant with Z provided")
    void shouldPreserveTimeWhenInstantWithZProvided() {
      // Given
      String instantStr = "2024-01-15T10:30:45Z";

      // When
      Instant result = DateUtils.parseFlexibleDateEndOfDay(instantStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T10:30:45Z");
    }

    @Test
    @DisplayName("Should parse legacy format to end of day")
    void shouldParseLegacyFormatToEndOfDay() {
      // Given
      String legacyDateStr = "15-01-2024";

      // When
      Instant result = DateUtils.parseFlexibleDateEndOfDay(legacyDateStr);

      // Then
      assertThat(result).isNotNull();
      String resultStr = result.atZone(ZoneId.of("UTC")).toString();
      assertThat(resultStr).startsWith("2024-01-15T23:59:59.999999999");
    }

    @Test
    @DisplayName("Should return null for null input")
    void shouldReturnNullForNullInput() {
      // When
      Instant result = DateUtils.parseFlexibleDateEndOfDay(null);

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for blank input")
    void shouldReturnNullForBlankInput() {
      // When
      Instant result = DateUtils.parseFlexibleDateEndOfDay("   ");

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle date at year boundary")
    void shouldHandleDateAtYearBoundary() {
      // Given
      String dateStr = "2024-12-31";

      // When
      Instant result = DateUtils.parseFlexibleDateEndOfDay(dateStr);

      // Then
      assertThat(result).isNotNull();
      String resultStr = result.atZone(ZoneId.of("UTC")).toString();
      assertThat(resultStr).startsWith("2024-12-31T23:59:59.999999999");
    }
  }

  @Nested
  @DisplayName("Legacy Methods (Backward Compatibility)")
  class LegacyMethodTests {

    @Test
    @DisplayName("parseToStartOfDay should work with dd-MM-yyyy format")
    void parseToStartOfDayShouldWorkWithLegacyFormat() {
      // Given
      String dateStr = "15-01-2024";

      // When
      Instant result = DateUtils.parseToStartOfDay(dateStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T00:00:00Z");
    }

    @Test
    @DisplayName("parseToEndOfDay should work with dd-MM-yyyy format")
    void parseToEndOfDayShouldWorkWithLegacyFormat() {
      // Given
      String dateStr = "15-01-2024";

      // When
      Instant result = DateUtils.parseToEndOfDay(dateStr);

      // Then
      assertThat(result).isNotNull();
      String resultStr = result.atZone(ZoneId.of("UTC")).toString();
      assertThat(resultStr).startsWith("2024-01-15T23:59:59.999999999");
    }

    @Test
    @DisplayName("parseToStartOfDay should return null for null input")
    void parseToStartOfDayShouldReturnNullForNull() {
      assertThat(DateUtils.parseToStartOfDay(null)).isNull();
    }

    @Test
    @DisplayName("parseToEndOfDay should return null for null input")
    void parseToEndOfDayShouldReturnNullForNull() {
      assertThat(DateUtils.parseToEndOfDay(null)).isNull();
    }

    @Test
    @DisplayName("parseToStartOfDay should throw exception for invalid format")
    void parseToStartOfDayShouldThrowExceptionForInvalidFormat() {
      assertThatThrownBy(() -> DateUtils.parseToStartOfDay("2024-01-15"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid date format");
    }
  }

  @Nested
  @DisplayName("Edge Cases and Special Scenarios")
  class EdgeCaseTests {

    @Test
    @DisplayName("Should handle leap year date")
    void shouldHandleLeapYearDate() {
      // Given
      String leapYearDate = "2024-02-29";

      // When
      Instant result = DateUtils.parseFlexibleDate(leapYearDate);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-02-29T00:00:00Z");
    }

    @Test
    @DisplayName("Should handle first day of year")
    void shouldHandleFirstDayOfYear() {
      // Given
      String firstDay = "2024-01-01";

      // When
      Instant result = DateUtils.parseFlexibleDate(firstDay);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-01T00:00:00Z");
    }

    @Test
    @DisplayName("Should handle last day of year")
    void shouldHandleLastDayOfYear() {
      // Given
      String lastDay = "2024-12-31";

      // When
      Instant result = DateUtils.parseFlexibleDateEndOfDay(lastDay);

      // Then
      assertThat(result).isNotNull();
      String resultStr = result.atZone(ZoneId.of("UTC")).toString();
      assertThat(resultStr).startsWith("2024-12-31T23:59:59.999999999");
    }

    @Test
    @DisplayName("Should handle dates far in the past")
    void shouldHandleDatesInPast() {
      // Given
      String oldDate = "2000-01-01";

      // When
      Instant result = DateUtils.parseFlexibleDate(oldDate);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2000-01-01T00:00:00Z");
    }

    @Test
    @DisplayName("Should handle dates in the future")
    void shouldHandleDatesInFuture() {
      // Given
      String futureDate = "2030-12-31";

      // When
      Instant result = DateUtils.parseFlexibleDate(futureDate);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2030-12-31T00:00:00Z");
    }

    @Test
    @DisplayName("Should reject invalid month")
    void shouldRejectInvalidMonth() {
      // Given
      String invalidMonth = "2024-13-01";

      // When & Then
      assertThatThrownBy(() -> DateUtils.parseFlexibleDate(invalidMonth))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid date/time format");
    }

    @Test
    @DisplayName("Should reject completely invalid date format")
    void shouldRejectCompletelyInvalidDateFormat() {
      // Given
      String invalidDay = "2024-99-99"; // Completely invalid

      // When & Then
      assertThatThrownBy(() -> DateUtils.parseFlexibleDate(invalidDay))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid date/time format");
    }

    @Test
    @DisplayName("Should reject invalid hour")
    void shouldRejectInvalidHour() {
      // Given
      String invalidHour = "2024-01-15T25:00:00";

      // When & Then
      assertThatThrownBy(() -> DateUtils.parseFlexibleDate(invalidHour))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid date/time format");
    }

    @Test
    @DisplayName("Should reject invalid minute")
    void shouldRejectInvalidMinute() {
      // Given
      String invalidMinute = "2024-01-15T10:60:00";

      // When & Then
      assertThatThrownBy(() -> DateUtils.parseFlexibleDate(invalidMinute))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid date/time format");
    }
  }

  @Nested
  @DisplayName("Date Range Scenarios (From/To)")
  class DateRangeTests {

    @Test
    @DisplayName("Should create valid range for same day with start and end")
    void shouldCreateValidRangeForSameDay() {
      // Given
      String dateStr = "2024-01-15";

      // When
      Instant from = DateUtils.parseFlexibleDate(dateStr);
      Instant to = DateUtils.parseFlexibleDateEndOfDay(dateStr);

      // Then
      assertThat(from).isNotNull();
      assertThat(to).isNotNull();
      assertThat(from).isBefore(to);
      assertThat(from.toString()).isEqualTo("2024-01-15T00:00:00Z");
      String toStr = to.atZone(ZoneId.of("UTC")).toString();
      assertThat(toStr).startsWith("2024-01-15T23:59:59.999999999");
    }

    @Test
    @DisplayName("Should create valid range for multiple days")
    void shouldCreateValidRangeForMultipleDays() {
      // Given
      String fromStr = "2024-01-01";
      String toStr = "2024-01-31";

      // When
      Instant from = DateUtils.parseFlexibleDate(fromStr);
      Instant to = DateUtils.parseFlexibleDateEndOfDay(toStr);

      // Then
      assertThat(from).isNotNull();
      assertThat(to).isNotNull();
      assertThat(from).isBefore(to);
      assertThat(from.toString()).isEqualTo("2024-01-01T00:00:00Z");
      String toResultStr = to.atZone(ZoneId.of("UTC")).toString();
      assertThat(toResultStr).startsWith("2024-01-31T23:59:59.999999999");
    }

    @Test
    @DisplayName("Should handle time-specific ranges")
    void shouldHandleTimeSpecificRanges() {
      // Given
      String fromStr = "2024-01-15T09:00:00";
      String toStr = "2024-01-15T17:00:00";

      // When
      Instant from = DateUtils.parseFlexibleDate(fromStr);
      Instant to = DateUtils.parseFlexibleDateEndOfDay(toStr); // Preserves time when T present

      // Then
      assertThat(from).isNotNull();
      assertThat(to).isNotNull();
      assertThat(from).isBefore(to);
      assertThat(from.toString()).isEqualTo("2024-01-15T09:00:00Z");
      assertThat(to.toString()).isEqualTo("2024-01-15T17:00:00Z");
    }
  }

  @Nested
  @DisplayName("Format Detection Priority")
  class FormatDetectionTests {

    @Test
    @DisplayName("Should prioritize ISO instant over other formats")
    void shouldPrioritizeIsoInstant() {
      // Given - valid ISO instant with Z
      String instantStr = "2024-01-15T10:30:00Z";

      // When
      Instant result = DateUtils.parseFlexibleDate(instantStr);

      // Then - should parse as instant, not try other formats
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T10:30:00Z");
    }

    @Test
    @DisplayName("Should try ISO datetime when instant fails")
    void shouldTryIsoDateTimeWhenInstantFails() {
      // Given - ISO datetime without Z
      String dateTimeStr = "2024-01-15T10:30:00";

      // When
      Instant result = DateUtils.parseFlexibleDate(dateTimeStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T10:30:00Z");
    }

    @Test
    @DisplayName("Should try ISO date when datetime fails")
    void shouldTryIsoDateWhenDateTimeFails() {
      // Given - ISO date only
      String dateStr = "2024-01-15";

      // When
      Instant result = DateUtils.parseFlexibleDate(dateStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T00:00:00Z");
    }

    @Test
    @DisplayName("Should fall back to legacy format when ISO fails")
    void shouldFallBackToLegacyFormatWhenIsoFails() {
      // Given - legacy dd-MM-yyyy format
      String legacyStr = "15-01-2024";

      // When
      Instant result = DateUtils.parseFlexibleDate(legacyStr);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.toString()).isEqualTo("2024-01-15T00:00:00Z");
    }
  }

  @Nested
  @DisplayName("formatToDate() - Date Formatting")
  class FormatToDateTests {

    @Test
    @DisplayName("Should format instant to dd-MM-yyyy")
    void shouldFormatInstantToLegacyFormat() {
      // Given
      Instant instant = Instant.parse("2024-01-15T10:30:00Z");

      // When
      String result = DateUtils.formatToDate(instant);

      // Then
      assertThat(result).isEqualTo("15-01-2024");
    }

    @Test
    @DisplayName("Should return null for null instant")
    void shouldReturnNullForNullInstant() {
      // When
      String result = DateUtils.formatToDate(null);

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should format to date ignoring time")
    void shouldFormatToDateIgnoringTime() {
      // Given
      Instant instant = Instant.parse("2024-01-15T23:59:59Z");

      // When
      String result = DateUtils.formatToDate(instant);

      // Then
      assertThat(result).isEqualTo("15-01-2024");
    }
  }
}
