package com.example.backend.common.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

public final class TestUtils {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  static {
    MAPPER.registerModule(new JavaTimeModule());
    // Configure the mapper with common settings
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  private TestUtils() {}

  /**
   * Converts an object to JSON string.
   *
   * @param object Object to convert
   * @return JSON string representation
   * @throws RuntimeException if conversion fails
   */
  public static String toJsonString(Object object) {
    try {
      return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert object to JSON", e);
    }
  }

  /**
   * Converts JSON string to specified class type.
   *
   * @param json JSON string to convert
   * @param clazz Class type to convert to
   * @return Object of specified class type
   * @throws RuntimeException if conversion fails
   */
  public static <T> T fromJson(String json, Class<T> clazz) {
    try {
      return MAPPER.readValue(json, clazz);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert JSON to object", e);
    }
  }

  /**
   * Get the ObjectMapper instance.
   *
   * @return The configured ObjectMapper
   */
  public static ObjectMapper getMapper() {
    return MAPPER;
  }
}
