package com.example.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.dto.DeletionStatus;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserFilter;
import com.example.backend.user.dto.UserStatisticsDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private UserMapper userMapper;

  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  private User testUser;
  private UserDto testUserDto;
  private CreateUserRequest createUserRequest;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .id(1L)
            .username("testuser")
            .passwordHash("$2a$10$hashedpassword")
            .firstName("Test")
            .lastName("User")
            .email("test@example.com")
            .role(UserRole.USER)
            .isActive(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    testUserDto =
        UserDto.builder()
            .id(1L)
            .username("testuser")
            .firstName("Test")
            .lastName("User")
            .email("test@example.com")
            .role(UserRole.USER)
            .isActive(true)
            .permissions(new HashSet<>())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    createUserRequest =
        CreateUserRequest.builder()
            .username("newuser")
            .password("Password123!")
            .email("newuser@example.com")
            .firstName("New")
            .lastName("User")
            .userRole(UserRole.USER)
            .build();
  }

  @Test
  @DisplayName("Register user - should create new user successfully")
  void registerUser_WithValidData_ShouldCreateUser() {
    // Given
    User newUser =
        User.builder()
            .id(10L)
            .username("newuser")
            .email("newuser@example.com")
            .firstName("New")
            .lastName("User")
            .phone("8681021923")
            .role(UserRole.USER)
            .isActive(true)
            .build();

    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
    when(userMapper.toEntity(createUserRequest)).thenReturn(newUser);
    when(passwordEncoder.encode("Password123!")).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(newUser);

    // When
    User result = userService.registerUser(createUserRequest);

    // Then
    assertNotNull(result);
    assertEquals("newuser", result.getUsername());
    assertEquals("newuser@example.com", result.getEmail());
    assertEquals(UserRole.USER, result.getRole());
    assertEquals("8681021923", result.getPhone());
    assertTrue(result.getIsActive());
    verify(userRepository).existsByUsername("newuser");
    verify(userRepository).existsByEmail("newuser@example.com");
    verify(passwordEncoder).encode("Password123!");
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("Register user - should throw exception when username exists")
  void registerUser_WithDuplicateUsername_ShouldThrowException() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.registerUser(createUserRequest))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Username already exists: newuser");

    verify(userRepository).existsByUsername("newuser");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Register user - should throw exception when email exists")
  void registerUser_WithDuplicateEmail_ShouldThrowException() {
    // Given
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.registerUser(createUserRequest))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Email already exists: newuser@example.com");

    verify(userRepository).existsByUsername("newuser");
    verify(userRepository).existsByEmail("newuser@example.com");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Load user by username - should return user when exists")
  void loadUserByUsernameWhenUserExistsShouldReturnUser() {
    when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    User result = userService.loadUserByUsername("testuser");
    assertThat(result).isNotNull();
    assertThat(result.getUsername()).isEqualTo("testuser");
    verify(userRepository).findByUsername("testuser");
  }

  @Test
  @DisplayName("Load user by username - should throw exception when not found")
  void loadUserByUsernameWhenUserNotFoundShouldThrowException() {
    when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("User not found with username: nonexistent");
    verify(userRepository).findByUsername("nonexistent");
  }

  @Test
  @DisplayName("Load user by username - should throw exception when user is inactive")
  void loadUserByUsernameWhenUserIsInactiveShouldThrowException() {
    User inactiveUser = createTestUser();
    inactiveUser.setIsActive(false);
    inactiveUser.setDeletedAt(Instant.now());
    inactiveUser.setDeletedBy("admin");

    when(userRepository.findByUsername("inactiveuser")).thenReturn(Optional.of(inactiveUser));

    assertThatThrownBy(() -> userService.loadUserByUsername("inactiveuser"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Account is inactive or has been deleted");
  }

  @Test
  @DisplayName("Load user by username - should block all operations for inactive users")
  void loadUserByUsernameForInactiveUserShouldBlockAllOperations() {
    // Simulate: User has valid JWT but was deactivated by admin
    User deactivatedUser = createTestUser();
    deactivatedUser.setIsActive(false);
    deactivatedUser.setDeletedAt(Instant.now());
    deactivatedUser.setDeletedBy("admin");

    when(userRepository.findByUsername("deactivated")).thenReturn(Optional.of(deactivatedUser));

    // JWT filter will call loadUserByUsername() on every request
    // This should block the user from ANY operation
    assertThatThrownBy(() -> userService.loadUserByUsername("deactivated"))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessageContaining("Account is inactive or has been deleted");
  }

  @Test
  @DisplayName("Get user by ID - should return user DTO when exists")
  void getUserByIdWhenUserExistsShouldReturnUserDto() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userMapper.toDto(testUser)).thenReturn(testUserDto);
    UserDto result = userService.getUserById(1L);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(1L);
    verify(userRepository).findById(1L);
    verify(userMapper).toDto(testUser);
  }

  @Test
  @DisplayName("Get user by ID - should throw exception when not found")
  void getUserByIdWhenUserNotFoundShouldThrowException() {
    when(userRepository.findById(999L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userService.getUserById(999L))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(userRepository).findById(999L);
  }

  @Test
  @DisplayName("Get all users - should return paginated users")
  void getAllUsersWithoutFiltersShouldReturnPaginatedUsers() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<User> userPage = new PageImpl<>(List.of(testUser));
    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
    when(userMapper.toDto(testUser)).thenReturn(testUserDto);

    // Updated to match new signature with date range filters
    UserFilter filter = UserFilter.builder().build(); // Empty filter
    Page<UserDto> result = userService.getAllUsers(filter, pageable);

    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    verify(userRepository).findAll(any(Specification.class), eq(pageable));
  }

  @Test
  @DisplayName("Update user - should update and return updated user")
  void updateUserWithValidDataShouldReturnUpdatedUser() {
    UserDto updateDto = UserDto.builder().firstName("Updated").build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);
    when(userMapper.toDto(testUser)).thenReturn(testUserDto);
    UserDto result = userService.updateUser(1L, updateDto);
    assertThat(result).isNotNull();
    verify(userRepository).save(testUser);
  }

  @Test
  @DisplayName("Delete user - should soft delete when user exists")
  void deleteUserWhenUserExistsShouldSoftDeleteUser() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.deleteUser(1L);

    verify(userRepository).findById(1L);
    verify(userRepository).save(testUser);
    // Verify soft delete was called on the user
    assertThat(testUser.getDeletedAt()).isNotNull();
    assertThat(testUser.getDeletedBy()).isNotNull();
    assertThat(testUser.getIsActive()).isFalse();
  }

  @Test
  @DisplayName("Delete user - should throw exception when already deleted")
  void deleteUserWhenAlreadyDeletedShouldThrowException() {
    testUser.softDelete("admin");
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    assertThatThrownBy(() -> userService.deleteUser(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("User is already deleted");

    verify(userRepository).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Delete user - should throw exception when trying to delete own account")
  void deleteUserWhenTryingToDeleteOwnAccountShouldThrowException() {
    // Setup: User trying to delete themselves
    testUser.setUsername("currentuser");
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    // Mock SecurityContext to return "currentuser" as the authenticated user
    org.springframework.security.core.context.SecurityContext securityContext =
        mock(org.springframework.security.core.context.SecurityContext.class);
    org.springframework.security.core.Authentication authentication =
        mock(org.springframework.security.core.Authentication.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("currentuser");
    org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

    assertThatThrownBy(() -> userService.deleteUser(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("You cannot deactivate your own account");

    verify(userRepository).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Restore user - should restore when user is deleted")
  void restoreUserWhenUserIsDeletedShouldRestoreUser() {
    testUser.softDelete("admin");
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    userService.restoreUser(1L);

    verify(userRepository).findById(1L);
    verify(userRepository).save(testUser);
    // Verify restore was called
    assertThat(testUser.getDeletedAt()).isNull();
    assertThat(testUser.getDeletedBy()).isNull();
  }

  @Test
  @DisplayName("Restore user - should throw exception when user is not deleted")
  void restoreUserWhenUserIsNotDeletedShouldThrowException() {
    // User is not deleted (deletedAt is null)
    when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

    assertThatThrownBy(() -> userService.restoreUser(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("User is not deleted");

    verify(userRepository).findById(1L);
    verify(userRepository, never()).save(any());
  }

  @Test
  @DisplayName("Get all users - should return only active users by default")
  void getAllUsersShouldReturnOnlyActiveUsersByDefault() {
    // Create mix of active and deleted users
    User activeUser = createTestUser();
    User deletedUser = createTestUser();
    deletedUser.setUsername("deleted");
    deletedUser.softDelete("admin");

    when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(activeUser)));

    UserFilter filter = UserFilter.builder().build(); // Default is ACTIVE_ONLY
    Page<UserDto> result = userService.getAllUsers(filter, Pageable.unpaged());

    assertThat(result.getContent()).hasSize(1);
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  @DisplayName("Get all users - should return only deleted users when DELETED_ONLY")
  void getAllUsersShouldReturnOnlyDeletedUsersWhenDeletedOnly() {
    User deletedUser = createTestUser();
    deletedUser.setUsername("deleted");
    deletedUser.softDelete("admin");

    when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(deletedUser)));

    UserFilter filter = UserFilter.builder().deletionStatus(DeletionStatus.DELETED_ONLY).build();
    Page<UserDto> result = userService.getAllUsers(filter, Pageable.unpaged());

    assertThat(result.getContent()).hasSize(1);
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  @Test
  @DisplayName("Get all users - should return all users when ALL")
  void getAllUsersShouldReturnAllUsersWhenAll() {
    // Create mix of active and deleted users
    User activeUser = createTestUser();
    User deletedUser = createTestUser();
    deletedUser.setUsername("deleted");
    deletedUser.softDelete("admin");

    when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(activeUser, deletedUser)));

    UserFilter filter = UserFilter.builder().deletionStatus(DeletionStatus.ALL).build();
    Page<UserDto> result = userService.getAllUsers(filter, Pageable.unpaged());

    assertThat(result.getContent()).hasSize(2);
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }

  /** Helper method to create a test user. */
  private User createTestUser() {
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setPasswordHash("hashedPassword");
    user.setEmail("test@example.com");
    user.setFirstName("Test");
    user.setLastName("User");
    user.setRole(UserRole.USER);
    user.setIsActive(true);
    return user;
  }

  @Test
  @DisplayName("Get user statistics - should return aggregate data")
  void getUserStatisticsShouldReturnAggregateData() {
    // Mock repository count methods
    when(userRepository.count()).thenReturn(150L);
    when(userRepository.countByIsActive(true)).thenReturn(120L);
    when(userRepository.countByIsActive(false)).thenReturn(30L);

    // Mock users by role
    List<Object[]> roleStats = new ArrayList<>();
    roleStats.add(new Object[] {UserRole.ADMIN, 5L});
    roleStats.add(new Object[] {UserRole.USER, 140L});
    roleStats.add(new Object[] {UserRole.GUEST, 5L});
    when(userRepository.countUsersByRole()).thenReturn(roleStats);

    UserStatisticsDto statistics = userService.getUserStatistics();

    assertThat(statistics.getTotalUsers()).isEqualTo(150L);
    assertThat(statistics.getTotalActiveUsers()).isEqualTo(120L);
    assertThat(statistics.getTotalInactiveUsers()).isEqualTo(30L);
    assertThat(statistics.getUsersByRole()).hasSize(3);
    assertThat(statistics.getUsersByRole().get("ADMIN")).isEqualTo(5L);
    assertThat(statistics.getUsersByRole().get("USER")).isEqualTo(140L);
    assertThat(statistics.getUsersByRole().get("GUEST")).isEqualTo(5L);
    assertThat(statistics.getUsersByStatus()).hasSize(2);
    assertThat(statistics.getUsersByStatus().get("ACTIVE")).isEqualTo(120L);
    assertThat(statistics.getUsersByStatus().get("INACTIVE")).isEqualTo(30L);

    verify(userRepository).countUsersByRole();
  }

  @Test
  @DisplayName("Get all users - should filter by ID range")
  void getAllUsersShouldFilterByIdRange() {
    User user1 = createTestUser();
    user1.setId(5L);
    User user2 = createTestUser();
    user2.setId(50L);
    user2.setUsername("user2");

    when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(user1, user2)));

    UserFilter filter = UserFilter.builder().idFrom(1L).idTo(100L).build();
    Page<UserDto> result = userService.getAllUsers(filter, Pageable.unpaged());

    assertThat(result.getContent()).hasSize(2);
    verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
  }
}
