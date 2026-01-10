package com.example.backend.user.controller;

import com.example.backend.common.BaseResponse;
import com.example.backend.user.dto.*;
import com.example.backend.user.entity.User;
import com.example.backend.user.mapper.UserMapper;
import com.example.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
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
  public ResponseEntity<BaseResponse<UserDto>> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    User user = userService.registerUser(request);
    UserDto userDto = userMapper.toDto(user);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            BaseResponse.success(
                userDto, "User created successfully. User must login to get access token."));
  }

  @GetMapping
  @Operation(summary = "Get all users", description = "Filter users using query parameters.")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<BaseResponse<Page<UserDto>>> getAllUsers(
      @ParameterObject UserFilterRequest request,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {

    log.debug("GET /api/v1/users - Request: {}\nPageable: {}", request, pageable.toString());
    UserFilter filter = userMapper.toFilter(request);
    Page<UserDto> users = userService.getAllUsers(filter, pageable);
    return ResponseEntity.ok(BaseResponse.success(users, "Users retrieved successfully"));
  }

  @PostMapping("/all")
  @Operation(
      summary = "Get ALL users without pagination",
      description =
          "Retrieves ALL users matching the filter criteria WITHOUT pagination. "
              + "Use with caution for large datasets (e.g., reports, exports). "
              + "Accepts same filters as GET /users but returns complete List instead of Page.")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<BaseResponse<List<UserDto>>> getAllUsersUnpaginated(
      @RequestBody UserFilterRequest request,
      @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
          Pageable pageable) {

    log.debug("POST /api/v1/users/all - Request: {}\nPageable: {}", request, pageable.toString());
    UserFilter filter = userMapper.toFilter(request);
    List<UserDto> users = userService.getAllUsersUnpaginated(filter, pageable);
    return ResponseEntity.ok(
        BaseResponse.success(users, users.size() + " users retrieved successfully"));
  }

  @GetMapping("/suggestions")
  @Operation(
      summary = "Get user suggestions for multiselect components",
      description =
          "Retrieves a lightweight list of users matching the search term for use in UI elements like search bars and multiselect components.")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<BaseResponse<List<UserSuggestionDto>>> getUserSuggestions(
      @RequestParam String searchTerm, @RequestParam(defaultValue = "10") int limit) {
    log.debug("GET /api/v1/users/suggestions - searchTerm: {}, limit: {}", searchTerm, limit);
    List<UserSuggestionDto> suggestions = userService.getUserSuggestions(searchTerm, limit);
    return ResponseEntity.ok(
        BaseResponse.success(suggestions, "Suggestions retrieved successfully"));
  }

  @GetMapping("/statistics")
  @Operation(
      summary = "Get user statistics",
      description =
          "Retrieves aggregate statistics about users including total counts, counts by role, "
              + "and counts by status (active/inactive/deleted)")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<BaseResponse<UserStatisticsDto>> getUserStatistics() {
    log.debug("GET /api/v1/users/statistics");
    UserStatisticsDto statistics = userService.getUserStatistics();
    return ResponseEntity.ok(BaseResponse.success(statistics, "Statistics retrieved successfully"));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID", description = "Retrieves a specific user by their ID")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<BaseResponse<UserDto>> getUserById(@PathVariable Long id) {
    UserDto user = userService.getUserById(id);
    return ResponseEntity.ok(BaseResponse.success(user, "User retrieved successfully"));
  }

  @GetMapping("/username/{username}")
  @Operation(
      summary = "Get user by username",
      description = "Retrieves a specific user by their username")
  @PreAuthorize("hasAuthority('PERMISSION_READ')")
  public ResponseEntity<BaseResponse<UserDto>> getUserByUsername(@PathVariable String username) {
    UserDto user = userService.getUserByUsername(username);
    return ResponseEntity.ok(BaseResponse.success(user, "User retrieved successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user", description = "Updates an existing user")
  @PreAuthorize("hasAuthority('PERMISSION_ADMIN')")
  public ResponseEntity<BaseResponse<UserDto>> updateUser(
      @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
    UserDto updatedUser = userService.updateUser(id, request);
    return ResponseEntity.ok(BaseResponse.success(updatedUser, "User updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user", description = "Deletes a user by ID")
  @PreAuthorize("hasAuthority('PERMISSION_DELETE') or hasAuthority('PERMISSION_ADMIN')")
  public ResponseEntity<BaseResponse<UserDto>> deleteUser(@PathVariable Long id) {
    UserDto deletedUser = userService.deleteUser(id);
    return ResponseEntity.ok(BaseResponse.success(deletedUser, "User deleted successfully"));
  }

  @PatchMapping("/{id}/restore")
  @Operation(
      summary = "Restore deleted user",
      description = "Restores a soft-deleted user by clearing deletion fields")
  @PreAuthorize("hasAuthority('PERMISSION_ADMIN') or hasAuthority('PERMISSION_MANAGE_USERS')")
  public ResponseEntity<BaseResponse<UserDto>> restoreUser(@PathVariable Long id) {
    UserDto restoredUser = userService.restoreUser(id);
    return ResponseEntity.ok(BaseResponse.success(restoredUser, "User restored successfully"));
  }
}
