package com.example.backend.user.mapper;

import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** MapStruct mapper for User entity and DTOs. */
@Mapper(componentModel = "spring")
public interface UserMapper {

  /**
   * Converts User entity to UserDto.
   *
   * @param user the user entity
   * @return UserDto
   */
  @Mapping(target = "permissions", source = "additionalPermissions")
  UserDto toDto(User user);

  /**
   * Converts CreateUserRequest to User entity. Password will be set separately after hashing.
   *
   * @param request the registration request
   * @return User entity
   */
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "lastLoginDate", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "deletedBy", ignore = true)
  @Mapping(target = "isActive", constant = "true")
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "additionalPermissions", ignore = true)
  User toEntity(CreateUserRequest request);
}
