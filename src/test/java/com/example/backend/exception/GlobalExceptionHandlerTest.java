package com.example.backend.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.common.BaseResponse;
import com.example.backend.user.entity.User;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
  }

  @Test
  @DisplayName("Handle PropertyReferenceException - should return 400 with valid properties")
  void handlePropertyReferenceExceptionShouldReturn400WithValidProperties() {
    // Create a PropertyReferenceException by catching the exception from PropertyPath.from()
    TypeInformation<User> typeInfo = TypeInformation.of(User.class);

    PropertyReferenceException ex = null;
    try {
      PropertyPath.from("asca", typeInfo);
    } catch (PropertyReferenceException e) {
      ex = e;
    }

    assertThat(ex).isNotNull();

    ResponseEntity<BaseResponse<Map<String, String>>> response =
        exceptionHandler.handlePropertyReferenceException(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isFalse();
    assertThat(response.getBody().getMessage()).contains("Invalid sort/filter property");
    assertThat(response.getBody().getMessage()).contains("asca");
    assertThat(response.getBody().getData()).containsKey("invalidProperty");
    assertThat(response.getBody().getData()).containsKey("validProperties");
    assertThat(response.getBody().getData().get("invalidProperty")).isEqualTo("asca");
    assertThat(response.getBody().getData().get("validProperties"))
        .contains("username", "email", "createdAt");
  }

  @Test
  @DisplayName("Handle ResourceNotFoundException - should return 404")
  void handleResourceNotFoundExceptionShouldReturn404() {
    ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", "123");

    ResponseEntity<BaseResponse<Void>> response =
        exceptionHandler.handleResourceNotFoundException(ex, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isFalse();
    assertThat(response.getBody().getMessage()).contains("User not found with id: 123");
  }

  @Test
  @DisplayName("Handle DuplicateResourceException - should return 409")
  void handleDuplicateResourceExceptionShouldReturn409() {
    DuplicateResourceException ex = new DuplicateResourceException("Username already exists");

    ResponseEntity<BaseResponse<Void>> response =
        exceptionHandler.handleDuplicateResourceException(ex, null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isFalse();
    assertThat(response.getBody().getMessage()).contains("Username already exists");
  }
}
