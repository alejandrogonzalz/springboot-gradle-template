package com.example.backend.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration for JSON serialization/deserialization.
 *
 * <p>Configures support for Java 8 date/time types (Instant, LocalDateTime, etc.)
 */
@Configuration
public class JacksonConfig {

  @Bean
  public ObjectMapper objectMapper() {

    SimpleModule stringTrimModule = new SimpleModule();
    stringTrimModule.addDeserializer(
        String.class,
        new StdScalarDeserializer<String>(String.class) {
          @Override
          public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            String value = jp.getValueAsString();
            if (value == null) return null;

            String fieldName = jp.currentName();

            // Protection: Do not touch sensitive or long-form text fields
            if ("password".equalsIgnoreCase(fieldName)
                || "confirmPassword".equalsIgnoreCase(fieldName)
                || "description".equalsIgnoreCase(fieldName)) {
              return value;
            }

            // Normalization: Trim + Replace multiple spaces/tabs/newlines with a single space
            return value.trim().replaceAll("\\s+", " ");
          }
        });

    return Jackson2ObjectMapperBuilder.json()
        .modules(new JavaTimeModule(), stringTrimModule)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();
  }
}
