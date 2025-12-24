package com.example.backend.user.service;

import com.example.backend.common.utils.SpecificationUtils;
import com.example.backend.common.utils.TestUtils;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.user.dto.CreateUserRequest;
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
  public User registerUser(CreateUserRequest request) {
    log.info("Registering new user: {}", TestUtils.toJsonString(request));

    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("Username already exists: " + request.getUsername());
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("Email already exists: " + request.getEmail());
    }

    User user = userMapper.toEntity(request);
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
  public List<UserDto> getAllUsersUnpaginated(UserFilter filter) {
    log.debug("Fetching ALL users (unpaginated) with filter: {}", filter);
    Specification<User> spec = buildUserSpecification(filter);
    return userRepository.findAll(spec).stream().map(userMapper::toDto).toList();
  }

  /**
   * Builds a Specification for User filtering. Extracted to reuse in both paginated and unpaginated
   * methods.
   */
  private Specification<User> buildUserSpecification(UserFilter filter) {
    Specification<User> spec = Specification.where(null);

    // Apply deletion status filter (uses isActive field) - only if isActive not explicitly set
    if (filter.getIsActive() != null) {
      // isActive explicitly set - use that directly
      spec = spec.and(SpecificationUtils.equals("isActive", filter.getIsActive()));
    } else if (filter.getDeletionStatus() != null) {
      // isActive not set - use deletionStatus
      switch (filter.getDeletionStatus()) {
        case ACTIVE_ONLY:
          spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isActive")));
          break;
        case DELETED_ONLY:
          spec = spec.and((root, query, cb) -> cb.isFalse(root.get("isActive")));
          break;
        case ALL:
          // No filter on isActive - show all users
          break;
      }
    }

    // Apply ID range filter
    if (filter.getIdFrom() != null || filter.getIdTo() != null) {
      spec = spec.and(SpecificationUtils.between("id", filter.getIdFrom(), filter.getIdTo()));
    }

    if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("username", filter.getUsername()));
    }

    if (filter.getFirstName() != null && !filter.getFirstName().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("firstName", filter.getFirstName()));
    }

    if (filter.getLastName() != null && !filter.getLastName().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("lastName", filter.getLastName()));
    }

    if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("email", filter.getEmail()));
    }

    if (filter.getRoles() != null && !filter.getRoles().isEmpty()) {
      spec = spec.and(SpecificationUtils.in("role", filter.getRoles()));
    }

    if (filter.getPermissions() != null && !filter.getPermissions().isEmpty()) {
      spec =
          spec.and(
              (root, query, cb) -> root.join("additionalPermissions").in(filter.getPermissions()));
    }

    if (filter.getCreatedAtFrom() != null || filter.getCreatedAtTo() != null) {
      spec =
          spec.and(
              SpecificationUtils.between(
                  "createdAt", filter.getCreatedAtFrom(), filter.getCreatedAtTo()));
    }

    if (filter.getUpdatedAtFrom() != null || filter.getUpdatedAtTo() != null) {
      spec =
          spec.and(
              SpecificationUtils.between(
                  "updatedAt", filter.getUpdatedAtFrom(), filter.getUpdatedAtTo()));
    }

    if (filter.getLastLoginDateFrom() != null || filter.getLastLoginDateTo() != null) {
      spec =
          spec.and(
              SpecificationUtils.between(
                  "lastLoginDate", filter.getLastLoginDateFrom(), filter.getLastLoginDateTo()));
    }

    if (filter.getCreatedBy() != null && !filter.getCreatedBy().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("createdBy", filter.getCreatedBy()));
    }

    if (filter.getUpdatedBy() != null && !filter.getUpdatedBy().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("updatedBy", filter.getUpdatedBy()));
    }

    if (filter.getDeletedBy() != null && !filter.getDeletedBy().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("deletedBy", filter.getDeletedBy()));
    }

    return spec;
  }

  /**
   * Updates a user.
   *
   * @param id the user ID
   * @param userDto the updated user data
   * @return updated UserDto
   */
  @Transactional
  public UserDto updateUser(Long id, UserDto userDto) {
    log.info("Updating user with id: {}", id);
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));

    // Update fields
    if (userDto.getFirstName() != null) {
      user.setFirstName(userDto.getFirstName());
    }
    if (userDto.getLastName() != null) {
      user.setLastName(userDto.getLastName());
    }
    if (userDto.getEmail() != null) {
      user.setEmail(userDto.getEmail());
    }
    if (userDto.getIsActive() != null) {
      user.setIsActive(userDto.getIsActive());
    }
    if (userDto.getRole() != null) {
      user.setRole(userDto.getRole());
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
