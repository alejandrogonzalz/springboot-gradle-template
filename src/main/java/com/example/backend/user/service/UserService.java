package com.example.backend.user.service;

import com.example.backend.common.utils.SpecificationUtils;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

  @Override
  public User loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Loading user by username: {}", username);
    return userRepository
        .findByUsername(username)
        .orElseThrow(
            () -> new UsernameNotFoundException("User not found with username: " + username));
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
   * @param username optional username filter (contains)
   * @param email optional email filter (contains)
   * @param isActive optional active status filter
   * @param pageable pagination information
   * @return Page of UserDto
   */
  public Page<UserDto> getAllUsers(
      String username, String email, Boolean isActive, Pageable pageable) {
    log.debug(
        "Fetching all users with filters - username: {}, email: {}, isActive: {}",
        username,
        email,
        isActive);

    Specification<User> spec = Specification.where(null);

    if (username != null && !username.isBlank()) {
      spec = spec.and(SpecificationUtils.contains("username", username));
    }

    if (email != null && !email.isBlank()) {
      spec = spec.and(SpecificationUtils.contains("email", email));
    }

    if (isActive != null) {
      spec = spec.and(SpecificationUtils.equals("isActive", isActive));
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
    if (userDto.getPermissions() != null) {
      user.setPermissions(userDto.getPermissions());
    }

    User savedUser = userRepository.save(user);
    return userMapper.toDto(savedUser);
  }

  /**
   * Deletes a user.
   *
   * @param id the user ID
   */
  @Transactional
  public void deleteUser(Long id) {
    log.info("Deleting user with id: {}", id);
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User", "id", id.toString());
    }
    userRepository.deleteById(id);
  }
}
