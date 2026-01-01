package com.example.backend.audit.mapper;

import com.example.backend.audit.dto.AuditLogDto;
import com.example.backend.audit.entity.AuditLog;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for AuditLog entity and DTOs.
 *
 * <p>Follows the same pattern as UserMapper for consistency.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

  /**
   * Converts AuditLog entity to AuditLogDto.
   *
   * @param entity the AuditLog entity
   * @return AuditLogDto
   */
  AuditLogDto toDto(AuditLog entity);
}
