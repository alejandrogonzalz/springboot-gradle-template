package com.example.backend.audit.aop;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.backend.audit.entity.AuditLog;
import com.example.backend.audit.repository.AuditLogRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Unit tests for AuditAspect. */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditAspect Unit Tests")
class AuditAspectTest {

  @Mock private AuditLogRepository auditLogRepository;

  @Mock private ProceedingJoinPoint joinPoint;

  @Mock private MethodSignature methodSignature;

  @InjectMocks private AuditAspect auditAspect;

  private MockHttpServletRequest request;
  private User testUser;

  @BeforeEach
  void setUp() {
    // Setup test user
    testUser =
        User.builder()
            .id(1L)
            .username("testuser")
            .email("test@example.com")
            .firstName("Test")
            .lastName("User")
            .role(UserRole.USER)
            .isActive(true)
            .build();

    // Setup join point
    when(joinPoint.getArgs()).thenReturn(new Object[] {"arg1", "arg2"});
  }

  private void setupSecurityContext(String username) {
    Authentication authentication = mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn(username);

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  private void setupHttpRequest() {
    request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/users");
    request.setMethod("POST");
    request.setRemoteAddr("192.168.1.100");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
  }

  @Test
  @DisplayName("Should create audit log for successful operation")
  void shouldCreateAuditLogForSuccessfulOperation() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    setupHttpRequest();
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "User created", true);
    when(joinPoint.proceed()).thenReturn(testUser);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    Object result = auditAspect.auditMethod(joinPoint, auditable);

    // Then
    assertThat(result).isEqualTo(testUser);

    // Wait a bit for async operation (in tests it's still synchronous)
    Thread.sleep(100);

    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());

    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getUsername()).isEqualTo("testuser");
    assertThat(capturedLog.getOperation()).isEqualTo("CREATE_USER");
    assertThat(capturedLog.getEntityType()).isEqualTo("User");
    assertThat(capturedLog.getEntityId()).isEqualTo(1L);
    assertThat(capturedLog.getDescription()).isEqualTo("User created");
    assertThat(capturedLog.getIpAddress()).isEqualTo("192.168.1.100");
    assertThat(capturedLog.getRequestUri()).isEqualTo("/api/v1/users");
    assertThat(capturedLog.getHttpMethod()).isEqualTo("POST");
    assertThat(capturedLog.getSuccess()).isTrue();
    assertThat(capturedLog.getErrorMessage()).isNull();
  }

  @Test
  @DisplayName("Should create audit log for failed operation")
  void shouldCreateAuditLogForFailedOperation() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    setupHttpRequest();
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "User created", true);
    RuntimeException exception = new RuntimeException("Database error");
    when(joinPoint.proceed()).thenThrow(exception);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When & Then
    assertThatThrownBy(() -> auditAspect.auditMethod(joinPoint, auditable))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Database error");

    // Wait a bit for async operation
    Thread.sleep(100);

    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());

    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getUsername()).isEqualTo("testuser");
    assertThat(capturedLog.getOperation()).isEqualTo("CREATE_USER");
    assertThat(capturedLog.getEntityType()).isEqualTo("User");
    assertThat(capturedLog.getSuccess()).isFalse();
    assertThat(capturedLog.getErrorMessage()).isEqualTo("Database error");
  }

  @Test
  @DisplayName("Should extract entity ID from BaseEntity result")
  void shouldExtractEntityIdFromBaseEntity() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    Auditable auditable = createAuditableAnnotation("UPDATE_USER", "User", "", true);
    when(joinPoint.proceed()).thenReturn(testUser);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    auditAspect.auditMethod(joinPoint, auditable);

    // Wait for async
    Thread.sleep(100);

    // Then
    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getEntityId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("Should handle null result gracefully")
  void shouldHandleNullResult() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    Auditable auditable = createAuditableAnnotation("DELETE_USER", "User", "", true);
    when(joinPoint.proceed()).thenReturn(null);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    Object result = auditAspect.auditMethod(joinPoint, auditable);

    // Then
    assertThat(result).isNull();

    Thread.sleep(100);

    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getEntityId()).isNull();
    assertThat(capturedLog.getSuccess()).isTrue();
  }

  @Test
  @DisplayName("Should use anonymous when no authentication")
  void shouldUseAnonymousWhenNoAuthentication() throws Throwable {
    // Given
    SecurityContextHolder.clearContext(); // Clear authentication
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "", true);
    when(joinPoint.proceed()).thenReturn(testUser);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    auditAspect.auditMethod(joinPoint, auditable);

    // Then
    Thread.sleep(100);
    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getUsername()).isEqualTo("anonymous");
  }

  @Test
  @DisplayName("Should extract IP from X-Forwarded-For header")
  void shouldExtractIpFromXForwardedForHeader() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    setupHttpRequest();
    request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18, 150.172.238.178");
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "", true);
    when(joinPoint.proceed()).thenReturn(testUser);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    auditAspect.auditMethod(joinPoint, auditable);

    // Then
    Thread.sleep(100);
    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getIpAddress()).isEqualTo("203.0.113.195");
  }

  @Test
  @DisplayName("Should use default description when empty")
  void shouldUseDefaultDescriptionWhenEmpty() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "", true);
    when(joinPoint.proceed()).thenReturn(testUser);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    auditAspect.auditMethod(joinPoint, auditable);

    // Then
    Thread.sleep(100);
    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getDescription()).isEqualTo("CREATE_USER (ID: 1)");
  }

  @Test
  @DisplayName("Should not capture data when captureData is false")
  void shouldNotCaptureDataWhenCaptureDataIsFalse() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "", false);
    when(joinPoint.proceed()).thenReturn(testUser);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    auditAspect.auditMethod(joinPoint, auditable);

    // Then
    Thread.sleep(100);
    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getChanges()).isNull();
  }

  @Test
  @DisplayName("Should capture request and response data when captureData is true")
  void shouldCaptureRequestAndResponseDataWhenCaptureDataIsTrue() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "", true);
    when(joinPoint.proceed()).thenReturn(testUser);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    auditAspect.auditMethod(joinPoint, auditable);

    // Then
    Thread.sleep(100);
    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getChanges()).isNotNull();
    assertThat(capturedLog.getChanges()).contains("\"request\"");
    assertThat(capturedLog.getChanges()).contains("\"response\"");
  }

  @Test
  @DisplayName("Should handle no HTTP request context gracefully")
  void shouldHandleNoHttpRequestContextGracefully() throws Throwable {
    // Given
    RequestContextHolder.resetRequestAttributes(); // Clear request context
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "", true);
    when(joinPoint.proceed()).thenReturn(testUser);

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    auditAspect.auditMethod(joinPoint, auditable);

    // Then
    Thread.sleep(100);
    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getIpAddress()).isNull();
    assertThat(capturedLog.getRequestUri()).isNull();
    assertThat(capturedLog.getHttpMethod()).isNull();
  }

  @Test
  @DisplayName("Should not fail main operation if audit logging fails")
  void shouldNotFailMainOperationIfAuditLoggingFails() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "", true);
    when(joinPoint.proceed()).thenReturn(testUser);
    doThrow(new RuntimeException("Database connection failed"))
        .when(auditLogRepository)
        .save(any(AuditLog.class));

    // When
    Object result = auditAspect.auditMethod(joinPoint, auditable);

    // Then - main operation should succeed
    assertThat(result).isEqualTo(testUser);
    verify(joinPoint, times(1)).proceed();
  }

  @Test
  @DisplayName("Should include execution time in metadata")
  void shouldIncludeExecutionTimeInMetadata() throws Throwable {
    // Given
    setupSecurityContext("testuser");
    Auditable auditable = createAuditableAnnotation("CREATE_USER", "User", "", true);
    when(joinPoint.proceed())
        .thenAnswer(
            invocation -> {
              Thread.sleep(50); // Simulate some execution time
              return testUser;
            });

    ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);

    // When
    auditAspect.auditMethod(joinPoint, auditable);

    // Then
    Thread.sleep(100);
    verify(auditLogRepository, timeout(1000).times(1)).save(auditLogCaptor.capture());
    AuditLog capturedLog = auditLogCaptor.getValue();
    assertThat(capturedLog.getMetadata()).contains("executionTimeMs");
    assertThat(capturedLog.getMetadata()).matches(".*\"executionTimeMs\":\\s*\\d+.*");
  }

  /** Helper method to create Auditable annotation mock */
  private Auditable createAuditableAnnotation(
      String operation, String entityType, String description, boolean captureData) {
    Auditable auditable = mock(Auditable.class);
    when(auditable.operation()).thenReturn(operation);
    when(auditable.entityType()).thenReturn(entityType);
    when(auditable.description()).thenReturn(description);
    when(auditable.captureData()).thenReturn(captureData);
    return auditable;
  }
}
