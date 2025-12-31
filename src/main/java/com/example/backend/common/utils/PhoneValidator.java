package com.example.backend.common.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

/**
 * Utility class for phone number validation and formatting using Google's libphonenumber.
 *
 * <p>This class provides methods to:
 *
 * <ul>
 *   <li>Validate phone numbers
 *   <li>Format phone numbers to E.164 international format
 *   <li>Extract country codes from phone numbers
 * </ul>
 */
public class PhoneValidator {

  private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

  private PhoneValidator() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Validates if a phone number is valid according to international phone number standards.
   *
   * @param phoneNumber the phone number to validate (can be null or blank)
   * @return true if the phone number is valid or null/blank (optional field), false otherwise
   */
  public static boolean isValid(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      return true; // Optional field
    }

    try {
      PhoneNumber number = phoneUtil.parse(phoneNumber, null);
      return phoneUtil.isValidNumber(number);
    } catch (NumberParseException e) {
      return false;
    }
  }

  /**
   * Formats a phone number to E.164 international format (e.g., +14155552671).
   *
   * @param phoneNumber the phone number to format
   * @return the formatted phone number in E.164 format, or the original input if parsing fails
   */
  public static String formatToE164(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      return phoneNumber;
    }

    try {
      PhoneNumber number = phoneUtil.parse(phoneNumber, null);
      if (phoneUtil.isValidNumber(number)) {
        return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
      }
      return phoneNumber; // Return as-is if invalid
    } catch (NumberParseException e) {
      return phoneNumber; // Return as-is if parsing fails
    }
  }

  /**
   * Extracts the country code from a phone number (e.g., +1, +44, +86).
   *
   * @param phoneNumber the phone number to extract the country code from
   * @return the country code with + prefix, or null if parsing fails
   */
  public static String getCountryCode(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      return null;
    }

    try {
      PhoneNumber number = phoneUtil.parse(phoneNumber, null);
      return "+" + number.getCountryCode();
    } catch (NumberParseException e) {
      return null;
    }
  }

  /**
   * Formats a phone number for display in international format (e.g., +1 415-555-2671).
   *
   * @param phoneNumber the phone number to format
   * @return the formatted phone number for display, or the original input if parsing fails
   */
  public static String formatForDisplay(String phoneNumber) {
    if (phoneNumber == null || phoneNumber.isBlank()) {
      return phoneNumber;
    }

    try {
      PhoneNumber number = phoneUtil.parse(phoneNumber, null);
      if (phoneUtil.isValidNumber(number)) {
        return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
      }
      return phoneNumber;
    } catch (NumberParseException e) {
      return phoneNumber;
    }
  }
}
