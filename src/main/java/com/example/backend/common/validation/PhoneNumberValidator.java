package com.example.backend.common.validation;

import com.example.backend.common.utils.PhoneValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidPhoneNumber annotation.
 *
 * <p>Uses Google's libphonenumber library to validate phone numbers according to international
 * standards.
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

  @Override
  public void initialize(ValidPhoneNumber constraintAnnotation) {
    // No initialization needed
  }

  @Override
  public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
    return PhoneValidator.isValid(phoneNumber);
  }
}
