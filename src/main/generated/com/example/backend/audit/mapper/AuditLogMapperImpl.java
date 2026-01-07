package com.example.backend.audit.mapper;

import com.example.backend.audit.dto.AuditLogDto;
import com.example.backend.audit.dto.AuditLogFilter;
import com.example.backend.audit.dto.AuditLogFilterRequest;
import com.example.backend.audit.entity.AuditLog;
import com.example.backend.common.dto.ChartPointDto;
import com.example.backend.common.dto.ChartPointProjection;
import com.example.backend.common.utils.DateMappingUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-06T18:06:46-0600",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Amazon.com Inc.)"
)
@Component
public class AuditLogMapperImpl extends AuditLogMapper {

    @Autowired
    private DateMappingUtils dateMappingUtils;

    @Override
    public AuditLogDto toDto(AuditLog entity) {
        if ( entity == null ) {
            return null;
        }

        AuditLogDto.AuditLogDtoBuilder auditLogDto = AuditLogDto.builder();

        auditLogDto.id( entity.getId() );
        auditLogDto.username( entity.getUsername() );
        auditLogDto.operation( entity.getOperation() );
        auditLogDto.entityType( entity.getEntityType() );
        auditLogDto.entityId( entity.getEntityId() );
        auditLogDto.description( entity.getDescription() );
        auditLogDto.ipAddress( entity.getIpAddress() );
        auditLogDto.requestUri( entity.getRequestUri() );
        auditLogDto.httpMethod( entity.getHttpMethod() );
        auditLogDto.changes( entity.getChanges() );
        auditLogDto.metadata( entity.getMetadata() );
        auditLogDto.success( entity.getSuccess() );
        auditLogDto.errorMessage( entity.getErrorMessage() );
        auditLogDto.createdAt( entity.getCreatedAt() );
        auditLogDto.updatedAt( entity.getUpdatedAt() );

        return auditLogDto.build();
    }

    @Override
    public AuditLogFilter toFilter(AuditLogFilterRequest request) {
        if ( request == null ) {
            return null;
        }

        AuditLogFilter.AuditLogFilterBuilder auditLogFilter = AuditLogFilter.builder();

        auditLogFilter.createdAtFrom( dateMappingUtils.toStartOfDay( request.getCreatedAtFrom() ) );
        auditLogFilter.createdAtTo( dateMappingUtils.toEndOfDay( request.getCreatedAtTo() ) );
        auditLogFilter.username( request.getUsername() );
        auditLogFilter.operation( request.getOperation() );
        auditLogFilter.entityType( request.getEntityType() );
        auditLogFilter.entityId( request.getEntityId() );
        auditLogFilter.ipAddress( request.getIpAddress() );
        auditLogFilter.requestUri( request.getRequestUri() );
        auditLogFilter.httpMethod( request.getHttpMethod() );
        auditLogFilter.success( request.getSuccess() );
        List<String> list = request.getUsernames();
        if ( list != null ) {
            auditLogFilter.usernames( new ArrayList<String>( list ) );
        }
        List<String> list1 = request.getOperations();
        if ( list1 != null ) {
            auditLogFilter.operations( new ArrayList<String>( list1 ) );
        }
        List<String> list2 = request.getEntityTypes();
        if ( list2 != null ) {
            auditLogFilter.entityTypes( new ArrayList<String>( list2 ) );
        }

        return auditLogFilter.build();
    }

    @Override
    public ChartPointDto toChartPointDto(ChartPointProjection projection) {
        if ( projection == null ) {
            return null;
        }

        ChartPointDto chartPointDto = new ChartPointDto();

        chartPointDto.setLabel( projection.getLabel() );
        chartPointDto.setValue( projection.getValue() );

        return chartPointDto;
    }

    @Override
    public List<ChartPointDto> toChartPointDtoList(List<ChartPointProjection> projections) {
        if ( projections == null ) {
            return null;
        }

        List<ChartPointDto> list = new ArrayList<ChartPointDto>( projections.size() );
        for ( ChartPointProjection chartPointProjection : projections ) {
            list.add( toChartPointDto( chartPointProjection ) );
        }

        return list;
    }
}
