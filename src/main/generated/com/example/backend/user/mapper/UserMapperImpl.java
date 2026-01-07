package com.example.backend.user.mapper;

import com.example.backend.common.utils.DateMappingUtils;
import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.dto.UserDto;
import com.example.backend.user.dto.UserFilter;
import com.example.backend.user.dto.UserFilterRequest;
import com.example.backend.user.entity.Permission;
import com.example.backend.user.entity.User;
import com.example.backend.user.entity.UserRole;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-06T18:06:46-0600",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl extends UserMapper {

    @Autowired
    private DateMappingUtils dateMappingUtils;

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        Set<Permission> set = user.getAdditionalPermissions();
        if ( set != null ) {
            userDto.permissions( new LinkedHashSet<Permission>( set ) );
        }
        userDto.id( user.getId() );
        userDto.username( user.getUsername() );
        userDto.firstName( user.getFirstName() );
        userDto.lastName( user.getLastName() );
        userDto.email( user.getEmail() );
        userDto.phone( user.getPhone() );
        userDto.role( user.getRole() );
        userDto.isActive( user.getIsActive() );
        userDto.lastLoginDate( user.getLastLoginDate() );
        userDto.createdAt( user.getCreatedAt() );
        userDto.updatedAt( user.getUpdatedAt() );
        userDto.createdBy( user.getCreatedBy() );
        userDto.updatedBy( user.getUpdatedBy() );
        userDto.deletedAt( user.getDeletedAt() );
        userDto.deletedBy( user.getDeletedBy() );

        return userDto.build();
    }

    @Override
    public User toEntity(CreateUserRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder<?, ?> user = User.builder();

        user.username( request.getUsername() );
        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );
        user.email( request.getEmail() );
        user.phone( request.getPhone() );

        user.isActive( true );

        return user.build();
    }

    @Override
    public UserFilter toFilter(UserFilterRequest request) {
        if ( request == null ) {
            return null;
        }

        UserFilter.UserFilterBuilder userFilter = UserFilter.builder();

        userFilter.createdAtFrom( dateMappingUtils.toStartOfDay( request.getCreatedAtFrom() ) );
        userFilter.createdAtTo( dateMappingUtils.toEndOfDay( request.getCreatedAtTo() ) );
        userFilter.updatedAtFrom( dateMappingUtils.toStartOfDay( request.getUpdatedAtFrom() ) );
        userFilter.updatedAtTo( dateMappingUtils.toEndOfDay( request.getUpdatedAtTo() ) );
        userFilter.lastLoginDateFrom( dateMappingUtils.toStartOfDay( request.getLastLoginDateFrom() ) );
        userFilter.lastLoginDateTo( dateMappingUtils.toEndOfDay( request.getLastLoginDateTo() ) );
        userFilter.deletedAtFrom( dateMappingUtils.toStartOfDay( request.getDeletedAtFrom() ) );
        userFilter.deletedAtTo( dateMappingUtils.toEndOfDay( request.getDeletedAtTo() ) );
        userFilter.idFrom( request.getIdFrom() );
        userFilter.idTo( request.getIdTo() );
        userFilter.username( request.getUsername() );
        userFilter.firstName( request.getFirstName() );
        userFilter.lastName( request.getLastName() );
        userFilter.email( request.getEmail() );
        List<UserRole> list = request.getRoles();
        if ( list != null ) {
            userFilter.roles( new ArrayList<UserRole>( list ) );
        }
        List<Permission> list1 = request.getPermissions();
        if ( list1 != null ) {
            userFilter.permissions( new ArrayList<Permission>( list1 ) );
        }
        userFilter.isActive( request.getIsActive() );
        userFilter.phone( request.getPhone() );
        userFilter.createdBy( request.getCreatedBy() );
        userFilter.updatedBy( request.getUpdatedBy() );
        userFilter.deletedBy( request.getDeletedBy() );
        userFilter.deletionStatus( request.getDeletionStatus() );

        return userFilter.build();
    }
}
