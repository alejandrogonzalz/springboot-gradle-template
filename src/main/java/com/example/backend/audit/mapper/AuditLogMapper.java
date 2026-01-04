package com.example.backend.audit.mapper;

import com.example.backend.audit.dto.AuditLogDto;
import com.example.backend.audit.dto.AuditLogFilter;
import com.example.backend.audit.dto.AuditLogFilterRequest;
import com.example.backend.audit.entity.AuditLog;
import com.example.backend.common.dto.ChartPointDto;
import com.example.backend.common.dto.ChartPointProjection;
import com.example.backend.common.utils.DateMappingUtils;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for AuditLog entity and DTOs.
 *
 * <p>Follows the same pattern as UserMapper for consistency.
 */
@Mapper(componentModel = "spring", uses = DateMappingUtils.class)
public abstract class AuditLogMapper {

  /**
   * Converts AuditLog entity to AuditLogDto.
   *
   * @param entity the AuditLog entity
   * @return AuditLogDto
   */
  public abstract AuditLogDto toDto(AuditLog entity);

  @Mapping(target = "createdAtFrom", source = "createdAtFrom", qualifiedByName = "toStartOfDay")
  @Mapping(target = "createdAtTo", source = "createdAtTo", qualifiedByName = "toEndOfDay")
  public abstract AuditLogFilter toFilter(AuditLogFilterRequest request);

  /**
   * Maps a single projection result to a DTO. MapStruct automatically matches getLabel() to label
   * and getValue() to value.
   */
  public abstract ChartPointDto toChartPointDto(ChartPointProjection projection);

  /** Maps a list of projections to a list of DTOs. */
  public abstract List<ChartPointDto> toChartPointDtoList(List<ChartPointProjection> projections);
}
