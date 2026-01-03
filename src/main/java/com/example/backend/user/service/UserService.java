package com.example.backend.user.service;

import com.example.backend.audit.aop.Auditable;
import com.example.backend.common.specification.SpecificationBuilder;
import com.example.backend.common.utils.PhoneValidator;
import com.example.backend.common.utils.TestUtils;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.dto.UpdateUserRequest;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserFilter;
import com.example.backend.user.dto.UserStatisticsDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service class for User operations. */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  public User loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Loading user by username: {}", username);
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + username));

    if (!user.getIsActive()) {
      log.warn("Inactive user attempted to login: {}", username);
      throw new UsernameNotFoundException(
          "Account is inactive or has been deleted. Please contact an administrator.");
    }

    return user;
  }

  /**
   * Registers a new user.
   *
   * <p>Note: This method does NOT generate tokens. Registration is admin-only and users must login
   * separately after being created.
   *
   * @param request the registration request
   * @return the created user
   */
  @Transactional
  @Auditable(operation = "CREATE_USER", entityType = "User", description = "User registration")
  public User registerUser(CreateUserRequest request) {
    log.info("Registering new user: {}", TestUtils.toJsonString(request));

    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("Username already exists: " + request.getUsername());
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Email already exists: " + request.getEmail());
    }

    User user = userMapper.toEntity(request);

    // Format phone to E.164 if provided
    if (user.getPhone() != null && !user.getPhone().isBlank()) {
      user.setPhone(PhoneValidator.formatToE164(user.getPhone()));
    }
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(request.getUserRole());
    user.setIsActive(true);

    User savedUser = userRepository.save(user);
    log.info("User registered successfully with id: {}", savedUser.getId());

    return savedUser;
  }

  /**
   * Gets a user by ID.
   *
   * @param id the user ID
   * @return UserDto
   */
  public UserDto getUserById(Long id) {
    log.debug("Fetching user with id: {}", id);
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));
    return userMapper.toDto(user);
  }

  /**
   * Gets a user by username.
   *
   * @param username the username
   * @return UserDto
   */
  public UserDto getUserByUsername(String username) {
    log.debug("Fetching user with username: {}", username);
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    return userMapper.toDto(user);
  }

  /**
   * Gets all users with optional filtering and pagination using SpecificationUtils.
   *
   * @param filter user filter criteria with builder pattern
   * @param pageable pagination information
   * @return Page of UserDto
   */
  public Page<UserDto> getAllUsers(UserFilter filter, Pageable pageable) {
    log.debug("Fetching all users with filter: {}", filter);
    Specification<User> spec = buildUserSpecification(filter);
    return userRepository.findAll(spec, pageable).map(userMapper::toDto);
  }

  /**
   * Gets all users without pagination - returns ALL records matching filter. Use with caution for
   * large datasets.
   *
   * @param filter user filter criteria
   * @return List of all UserDto matching the filter
   */
  public List<UserDto> getAllUsersUnpaginated(UserFilter filter, Pageable pageable) {
    log.debug("Fetching ALL users (unpaginated) with filter: {}", filter);
    Sort sort = pageable.getSort();
    Specification<User> spec = buildUserSpecification(filter);
    return userRepository.findAll(spec, sort).stream().map(userMapper::toDto).toList();
  }

  /**
   * Builds a Specification for User filtering using the fluent builder pattern.
   *
   * <p>Eliminates boilerplate code by using {@link SpecificationBuilder} instead of manual
   * Specification.where(null).and(...) chains.
   */
  private Specification<User> buildUserSpecification(UserFilter filter) {
    SpecificationBuilder<User> builder = SpecificationBuilder.builder();

    // Apply deletion status filter (uses isActive field) - only if isActive not explicitly set
    if (filter.getIsActive() != null) {
      // isActive explicitly set - use that directly
      builder.equals("isActive", filter.getIsActive());
    } else if (filter.getDeletionStatus() != null) {
      // isActive not set - use deletionStatus
      switch (filter.getDeletionStatus()) {
        case ACTIVE_ONLY:
          builder.isTrue("isActive");
          break;
        case DELETED_ONLY:
          builder.isFalse("isActive");
          break;
        case ALL:
          // No filter on isActive - show all users
          break;
      }
    }

    // Apply all other filters using fluent builder
    return builder
        .between("id", filter.getIdFrom(), filter.getIdTo())
        .contains("username", filter.getUsername())
        .contains("firstName", filter.getFirstName())
        .contains("lastName", filter.getLastName())
        .contains("email", filter.getEmail())
        .contains("phone", filter.getPhone())
        .in("role", filter.getRoles())
        .joinIn("additionalPermissions", filter.getPermissions())
        .between("createdAt", filter.getCreatedAtFrom(), filter.getCreatedAtTo())
        .between("updatedAt", filter.getUpdatedAtFrom(), filter.getUpdatedAtTo())
        .between("lastLoginDate", filter.getLastLoginDateFrom(), filter.getLastLoginDateTo())
        .contains("createdBy", filter.getCreatedBy())
        .contains("updatedBy", filter.getUpdatedBy())
        .contains("deletedBy", filter.getDeletedBy())
        .between("deletedAt", filter.getDeletedAtFrom(), filter.getDeletedAtTo())
        .build();
  }

  /**
   * Updates a user.
   *
   * @param id the user ID
   * @return updated UserDto
   */
  @Transactional
  @Auditable(operation = "UPDATE_USER", entityType = "User", description = "User profile update")
  public UserDto updateUser(Long id, UpdateUserRequest request) {
    log.info("Updating user with id: {}", id);
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));

    // Update fields
    if (request.getFirstName() != null) {
      user.setFirstName(request.getFirstName());
    }
    if (request.getLastName() != null) {
      user.setLastName(request.getLastName());
    }
    if (request.getEmail() != null) {
      user.setEmail(request.getEmail());
    }
    if (request.getPhone() != null) {
      // Format phone to E.164 if not blank
      String phone =
          request.getPhone().isBlank() ? null : PhoneValidator.formatToE164(request.getPhone());
      user.setPhone(phone);
    }
    if (request.getIsActive() != null) {
      user.setIsActive(request.getIsActive());
    }
    if (request.getRole() != null) {
      user.setRole(request.getRole());
    }

    User savedUser = userRepository.save(user);
    return userMapper.toDto(savedUser);
  }

  /**
   * Soft deletes a user by setting deletedAt timestamp and marking as inactive. User data is
   * retained in the database for audit purposes.
   *
   * @param id the user ID
   * @return UserDto of the soft-deleted user
   * @throws ResourceNotFoundException if user not found or already deleted
   */
  @Transactional
  @Auditable(operation = "DELETE_USER", entityType = "User", description = "User soft delete")
  public UserDto deleteUser(Long id) {
    log.info("Soft deleting user with id: {}", id);

    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));

    if (user.isDeleted()) {
      throw new IllegalStateException("User is already deleted");
    }

    // Get current user for audit (deletedBy)
    String deletedBy = getCurrentUsername();

    // Prevent self-deactivation
    if (user.getUsername().equals(deletedBy)) {
      throw new IllegalStateException("You cannot deactivate your own account");
    }

    user.softDelete(deletedBy);
    User savedUser = userRepository.save(user);

    log.info("User soft deleted successfully - id: {}, deletedBy: {}", id, deletedBy);
    return userMapper.toDto(savedUser);
  }

  /** Gets the current authenticated username for audit purposes. */
  private String getCurrentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (authentication != null && authentication.getName() != null)
        ? authentication.getName()
        : "system";
  }

  /**
   * Restores a soft-deleted user.
   *
   * @param id the user ID
   * @return UserDto of the restored user
   * @throws ResourceNotFoundException if user not found
   */
  @Transactional
  @Auditable(operation = "RESTORE_USER", entityType = "User", description = "User restoration")
  public UserDto restoreUser(Long id) {
    log.info("Restoring user with id: {}", id);

    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));

    if (!user.isDeleted()) {
      throw new IllegalStateException("User is not deleted");
    }

    user.restore();
    User savedUser = userRepository.save(user);

    log.info("User restored successfully - id: {}", id);
    return userMapper.toDto(savedUser);
  }

  /**
   * Gets user table statistics.
   *
   * @return UserStatisticsDto containing aggregate data
   */
  public UserStatisticsDto getUserStatistics() {
    log.debug("Fetching user statistics");

    // Count total users (all statuses)
    long totalUsers = userRepository.count();

    // Count active and inactive users
    long totalActiveUsers = userRepository.countByIsActive(true);
    long totalInactiveUsers = userRepository.countByIsActive(false);

    // Count users by role (all statuses)
    Map<String, Long> usersByRole = new HashMap<>();
    List<Object[]> roleStats = userRepository.countUsersByRole();
    for (Object[] row : roleStats) {
      String role = row[0].toString();
      Long count = ((Number) row[1]).longValue();
      usersByRole.put(role, count);
    }

    // Build status map
    Map<String, Long> usersByStatus = new HashMap<>();
    usersByStatus.put("ACTIVE", totalActiveUsers);
    usersByStatus.put("INACTIVE", totalInactiveUsers);

    return UserStatisticsDto.builder()
        .totalUsers(totalUsers)
        .totalActiveUsers(totalActiveUsers)
        .totalInactiveUsers(totalInactiveUsers)
        .usersByRole(usersByRole)
        .usersByStatus(usersByStatus)
        .build();
  }
}
