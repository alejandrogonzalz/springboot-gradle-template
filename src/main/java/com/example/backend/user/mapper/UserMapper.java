package com.example.backend.user.mapper;

import com.example.backend.user.dto.RegisterRequest;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts User entity to UserDto.
     *
     * @param user the user entity
     * @return UserDto
     */
    UserDto toDto(User user);

    /**
     * Converts RegisterRequest to User entity.
     * Password will be set separately after hashing.
     *
     * @param request the registration request
     * @return User entity
     */
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "lastLoginDate", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    User toEntity(RegisterRequest request);
}
