package com.example.backend.user.service;

import com.example.backend.common.utils.SpecificationUtils;
import com.example.backend.common.utils.TestUtils;
import com.example.backend.exception.DuplicateResourceException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserFilter;
import com.example.backend.user.entity.User;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.repository.UserRepository;
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
    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () -> new UsernameNotFoundException("User not found with username: " + username));
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

    // By default, exclude soft-deleted users
    Specification<User> spec = Specification.where(null);
    spec = spec.and((root, query, cb) -> cb.isNull(root.get("deletedAt")));

    if (filter.getUsername() != null && !filter.getUsername().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("username", filter.getUsername()));
    }

    if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
      spec = spec.and(SpecificationUtils.contains("email", filter.getEmail()));
    }

    if (filter.getRoles() != null && !filter.getRoles().isEmpty()) {
      spec = spec.and(SpecificationUtils.in("role", filter.getRoles()));
    }

    if (filter.getIsActive() != null && !filter.getIsActive().isEmpty()) {
      spec = spec.and(SpecificationUtils.in("isActive", filter.getIsActive()));
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

    return userRepository.findAll(spec, pageable).map(userMapper::toDto);
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
   * @throws ResourceNotFoundException if user not found or already deleted
   */
  @Transactional
  public void deleteUser(Long id) {
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

    user.softDelete(deletedBy);
    userRepository.save(user);

    log.info("User soft deleted successfully - id: {}, deletedBy: {}", id, deletedBy);
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
   * @throws ResourceNotFoundException if user not found
   */
  @Transactional
  public void restoreUser(Long id) {
    log.info("Restoring user with id: {}", id);

    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id.toString()));

    if (!user.isDeleted()) {
      throw new IllegalStateException("User is not deleted");
    }

    user.restore();
    userRepository.save(user);

    log.info("User restored successfully - id: {}", id);
  }
}
