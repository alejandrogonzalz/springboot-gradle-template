package com.example.backend.user.mapper;

import com.example.backend.common.utils.DateMappingUtils;
import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserFilter;
import com.example.backend.user.dto.UserFilterRequest;
import com.example.backend.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User entity and DTOs. 1. Methods fpr MapStruct (Must be 'abstract') These do
 * NOT have { body } because MapStruct generates the code. 2. Helper methods (Must have { body })
 * These do NOT have 'abstract' because YOU are providing the logic.
 */
@Mapper(componentModel = "spring", uses = DateMappingUtils.class)
public abstract class UserMapper {

  /**
   * Converts User entity to UserDto.
   *
   * @param user the user entity
   * @return UserDto
   */
  @Mapping(target = "permissions", source = "additionalPermissions")
  public abstract UserDto toDto(User user);

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
  public abstract User toEntity(CreateUserRequest request);

  @Mapping(target = "createdAtFrom", source = "createdAtFrom", qualifiedByName = "toStartOfDay")
  @Mapping(target = "createdAtTo", source = "createdAtTo", qualifiedByName = "toEndOfDay")
  @Mapping(target = "updatedAtFrom", source = "updatedAtFrom", qualifiedByName = "toStartOfDay")
  @Mapping(target = "updatedAtTo", source = "updatedAtTo", qualifiedByName = "toEndOfDay")
  @Mapping(
      target = "lastLoginDateFrom",
      source = "lastLoginDateFrom",
      qualifiedByName = "toStartOfDay")
  @Mapping(target = "lastLoginDateTo", source = "lastLoginDateTo", qualifiedByName = "toEndOfDay")
  @Mapping(target = "deletedAtFrom", source = "deletedAtFrom", qualifiedByName = "toStartOfDay")
  @Mapping(target = "deletedAtTo", source = "deletedAtTo", qualifiedByName = "toEndOfDay")
  public abstract UserFilter toFilter(UserFilterRequest request);
}
