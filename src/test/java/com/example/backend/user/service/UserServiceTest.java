package com.example.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.repository.UserRepository;
import java.time.Instant;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private UserMapper userMapper;

  @InjectMocks private UserService userService;

  private User testUser;
  private UserDto testUserDto;

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
    Page<UserDto> result = userService.getAllUsers(null, null, null, pageable);
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
  @DisplayName("Delete user - should delete when user exists")
  void deleteUserWhenUserExistsShouldDeleteUser() {
    when(userRepository.existsById(1L)).thenReturn(true);
    doNothing().when(userRepository).deleteById(1L);
    userService.deleteUser(1L);
    verify(userRepository).existsById(1L);
    verify(userRepository).deleteById(1L);
  }
}
