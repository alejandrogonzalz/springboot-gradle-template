package com.example.backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation for phone numbers.
 *
 * <p>Validates phone numbers using Google's libphonenumber library. Accepts phone numbers in E.164
 * format (e.g., +14155552671) or other international formats.
 *
 * <p>Null and blank values are considered valid (use @NotNull/@NotBlank for required fields).
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface ValidPhoneNumber {

  String message() default
      "Invalid phone number format. Use international format (e.g., +14155552671)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
