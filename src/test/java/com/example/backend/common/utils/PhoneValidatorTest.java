package com.example.backend.common.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PhoneValidator Tests")
class PhoneValidatorTest {

  // ========== isValid() Tests ==========

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   "})
  @DisplayName("isValid() should return true for null, empty, or blank phone numbers")
  void isValid_ShouldReturnTrue_WhenPhoneIsNullOrBlank(String phone) {
    assertTrue(PhoneValidator.isValid(phone));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "+14155552671", // US
        "+442071234567", // UK
        "+861012345678", // China
        "+81312345678", // Japan
        "+33123456789", // France
        "+49301234567", // Germany
        "+5511987654321", // Brazil
        "+61212345678", // Australia
        "+919876543210" // India
      })
  @DisplayName("isValid() should return true for valid international phone numbers")
  void isValid_ShouldReturnTrue_WhenPhoneIsValid(String phone) {
    assertTrue(PhoneValidator.isValid(phone));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "123", // Too short
        "invalid", // Not a number
        "12345", // Invalid format
        "+1234", // Too short
        "0000000000", // Invalid pattern
        "abc123def456" // Mixed invalid
      })
  @DisplayName("isValid() should return false for invalid phone numbers")
  void isValid_ShouldReturnFalse_WhenPhoneIsInvalid(String phone) {
    assertFalse(PhoneValidator.isValid(phone));
  }

  // ========== formatToE164() Tests ==========

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   "})
  @DisplayName("formatToE164() should return input as-is for null, empty, or blank")
  void formatToE164_ShouldReturnAsIs_WhenPhoneIsNullOrBlank(String phone) {
    assertEquals(phone, PhoneValidator.formatToE164(phone));
  }

  @ParameterizedTest
  @CsvSource({
    "+1 415 555 2671, +14155552671",
    "+1-415-555-2671, +14155552671",
    "+1 (415) 555-2671, +14155552671",
    "+44 20 7123 4567, +442071234567",
    "+86 10 1234 5678, +861012345678",
    "+33 1 23 45 67 89, +33123456789"
  })
  @DisplayName("formatToE164() should format valid phones to E.164")
  void formatToE164_ShouldFormatToE164_WhenPhoneIsValid(String input, String expected) {
    assertEquals(expected, PhoneValidator.formatToE164(input));
  }

  @Test
  @DisplayName("formatToE164() should return input as-is when phone is invalid")
  void formatToE164_ShouldReturnAsIs_WhenPhoneIsInvalid() {
    String invalid = "invalid-phone";
    assertEquals(invalid, PhoneValidator.formatToE164(invalid));
  }

  // ========== getCountryCode() Tests ==========

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   "})
  @DisplayName("getCountryCode() should return null for null, empty, or blank")
  void getCountryCode_ShouldReturnNull_WhenPhoneIsNullOrBlank(String phone) {
    assertNull(PhoneValidator.getCountryCode(phone));
  }

  @ParameterizedTest
  @CsvSource({
    "+14155552671, +1", // US
    "+442071234567, +44", // UK
    "+861012345678, +86", // China
    "+81312345678, +81", // Japan
    "+33123456789, +33", // France
    "+49301234567, +49", // Germany
    "+5511987654321, +55", // Brazil
    "+61212345678, +61", // Australia
    "+919876543210, +91" // India
  })
  @DisplayName("getCountryCode() should extract country code from valid phone")
  void getCountryCode_ShouldExtractCode_WhenPhoneIsValid(String phone, String expectedCode) {
    assertEquals(expectedCode, PhoneValidator.getCountryCode(phone));
  }

  @Test
  @DisplayName("getCountryCode() should return null when phone is invalid")
  void getCountryCode_ShouldReturnNull_WhenPhoneIsInvalid() {
    assertNull(PhoneValidator.getCountryCode("invalid-phone"));
  }

  // ========== formatForDisplay() Tests ==========

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "   "})
  @DisplayName("formatForDisplay() should return input as-is for null, empty, or blank")
  void formatForDisplay_ShouldReturnAsIs_WhenPhoneIsNullOrBlank(String phone) {
    assertEquals(phone, PhoneValidator.formatForDisplay(phone));
  }

  @Test
  @DisplayName("formatForDisplay() should format to international display format")
  void formatForDisplay_ShouldFormatToInternational_WhenPhoneIsValid() {
    String phone = "+14155552671";
    String formatted = PhoneValidator.formatForDisplay(phone);

    assertNotNull(formatted);
    assertTrue(formatted.contains("+1")); // Should contain country code
    assertTrue(formatted.length() > phone.length()); // Should add formatting
  }

  @Test
  @DisplayName("formatForDisplay() should return input as-is when phone is invalid")
  void formatForDisplay_ShouldReturnAsIs_WhenPhoneIsInvalid() {
    String invalid = "invalid-phone";
    assertEquals(invalid, PhoneValidator.formatForDisplay(invalid));
  }

  // ========== Integration Tests ==========

  @Test
  @DisplayName("Should handle complete workflow: validate -> format -> extract country code")
  void completeWorkflow_ShouldWorkCorrectly() {
    String inputPhone = "+1 (415) 555-2671";

    // 1. Validate
    assertTrue(PhoneValidator.isValid(inputPhone));

    // 2. Format to E.164
    String e164 = PhoneValidator.formatToE164(inputPhone);
    assertEquals("+14155552671", e164);

    // 3. Extract country code
    String countryCode = PhoneValidator.getCountryCode(e164);
    assertEquals("+1", countryCode);

    // 4. Format for display
    String display = PhoneValidator.formatForDisplay(e164);
    assertNotNull(display);
    assertTrue(display.contains("+1"));
  }
}
