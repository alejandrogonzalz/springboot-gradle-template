package com.example.backend.user.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.common.utils.DateUtils;
import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserFilter;
import com.example.backend.user.dto.UserStatisticsDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management operations. Uses Spring Security's @PreAuthorize with
 * authority-based access control.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management operations")
public class UserController {

  private final UserService userService;
  private final UserMapper userMapper;

  @PostMapping
  @Operation(
      summary = "Create a new user (Admin only)",
      description =
          "Creates a new user account. Only administrators can register new users. User must login separately after creation.")
  @PreAuthorize("hasAuthority('PERMISSION_MANAGE_USERS') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserDto>> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    User user = userService.registerUser(request);
    UserDto userDto = userMapper.toDto(user);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                userDto, "User created successfully. User must login to get access token."));
  }

  @GetMapping
  @Operation(
      summary = "Get all users",
      description =
          "Retrieves all users with optional filtering and pagination. "
              + "Supports filtering by: username (contains), email (contains), roles (in list), active status (in list), "
              + "and date ranges (createdAt, updatedAt, lastLoginDate) in dd-MM-yyyy format. "
              + "Example: /api/v1/users?roles=ADMIN,USER&isActive=true&createdAtFrom=01-01-2024&createdAtTo=31-12-2024")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
      @Parameter(description = "Filter by username (contains)") @RequestParam(required = false)
          String username,
      @Parameter(description = "Filter by email (contains)") @RequestParam(required = false)
          String email,
      @Parameter(description = "Filter by roles (e.g., ADMIN,USER)") @RequestParam(required = false)
          List<UserRole> roles,
      @Parameter(description = "Filter by active status (e.g., true,false)")
          @RequestParam(required = false)
          List<Boolean> isActive,
      @Parameter(description = "Created date from (dd-MM-yyyy, e.g., 01-01-2024)")
          @RequestParam(required = false)
          String createdAtFrom,
      @Parameter(description = "Created date to (dd-MM-yyyy, e.g., 31-12-2024)")
          @RequestParam(required = false)
          String createdAtTo,
      @Parameter(description = "Updated date from (dd-MM-yyyy)") @RequestParam(required = false)
          String updatedAtFrom,
      @Parameter(description = "Updated date to (dd-MM-yyyy)") @RequestParam(required = false)
          String updatedAtTo,
      @Parameter(description = "Last login date from (dd-MM-yyyy)") @RequestParam(required = false)
          String lastLoginDateFrom,
      @Parameter(description = "Last login date to (dd-MM-yyyy)") @RequestParam(required = false)
          String lastLoginDateTo,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {

    // Parse date strings to Instant
    Instant createdAtFromInstant = DateUtils.parseToStartOfDay(createdAtFrom);
    Instant createdAtToInstant = DateUtils.parseToEndOfDay(createdAtTo);
    Instant updatedAtFromInstant = DateUtils.parseToStartOfDay(updatedAtFrom);
    Instant updatedAtToInstant = DateUtils.parseToEndOfDay(updatedAtTo);
    Instant lastLoginDateFromInstant = DateUtils.parseToStartOfDay(lastLoginDateFrom);
    Instant lastLoginDateToInstant = DateUtils.parseToEndOfDay(lastLoginDateTo);

    // Build filter object
    UserFilter filter =
        UserFilter.builder()
            .username(username)
            .email(email)
            .roles(roles)
            .isActive(isActive)
            .createdAtFrom(createdAtFromInstant)
            .createdAtTo(createdAtToInstant)
            .updatedAtFrom(updatedAtFromInstant)
            .updatedAtTo(updatedAtToInstant)
            .lastLoginDateFrom(lastLoginDateFromInstant)
            .lastLoginDateTo(lastLoginDateToInstant)
            .build();

    log.debug("GET /api/v1/users - Filter: {}", filter);

    Page<UserDto> users = userService.getAllUsers(filter, pageable);
    return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
  }

  @PostMapping("/all")
  @Operation(
      summary = "Get ALL users without pagination",
      description =
          "Retrieves ALL users matching the filter criteria WITHOUT pagination. "
              + "Use with caution for large datasets (e.g., reports, exports). "
              + "Accepts same filters as GET /users but returns complete List instead of Page.")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsersUnpaginated(
      @RequestBody UserFilter filter) {

    log.debug("POST /api/v1/users/all - Filter: {}", filter);

    List<UserDto> users = userService.getAllUsersUnpaginated(filter);
    return ResponseEntity.ok(
        ApiResponse.success(users, users.size() + " users retrieved successfully"));
  }

  @GetMapping("/statistics")
  @Operation(
      summary = "Get user statistics",
      description =
          "Retrieves aggregate statistics about users including total counts, counts by role, "
              + "and counts by status (active/inactive/deleted)")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<UserStatisticsDto>> getUserStatistics() {
    log.debug("GET /api/v1/users/statistics");
    UserStatisticsDto statistics = userService.getUserStatistics();
    return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
    UserDto user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
  }

  @GetMapping("/username/{username}")
  @Operation(
      summary = "Get user by username",
      description = "Retrieves a specific user by their username")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<ApiResponse<UserDto>> getUserByUsername(@PathVariable String username) {
    UserDto user = userService.getUserByUsername(username);
    return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user", description = "Updates an existing user")
  @PreAuthorize("hasAuthority('PERMISSION_UPDATE') or #id == authentication.principal.id")
  public ResponseEntity<ApiResponse<UserDto>> updateUser(
      @PathVariable Long id, @RequestBody UserDto userDto) {
    UserDto updatedUser = userService.updateUser(id, userDto);
    return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user", description = "Deletes a user by ID")
  @PreAuthorize("hasAuthority('PERMISSION_DELETE') or hasAuthority('PERMISSION_ADMIN')")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
  }

  @PostMapping("/{id}/restore")
  @Operation(
      summary = "Restore deleted user",
      description = "Restores a soft-deleted user by clearing deletion fields")
  @PreAuthorize("hasAuthority('PERMISSION_ADMIN') or hasAuthority('PERMISSION_MANAGE_USERS')")
  public ResponseEntity<ApiResponse<UserDto>> restoreUser(@PathVariable Long id) {
    userService.restoreUser(id);
    UserDto user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(user, "User restored successfully"));
  }
}
