# Backend Examples

## Canonical Reference Files

### Controllers
- **Primary**: `src/main/java/com/example/backend/user/controller/UserController.java`
  - Demonstrates: paginated endpoints, filter building, @PreAuthorize, comprehensive @Parameter documentation
- **Secondary**: `src/main/java/com/example/backend/audit/controller/AuditLogController.java`
  - Demonstrates: admin-only endpoint, DTO responses, Instant parameters

### Services
- **Primary**: `src/main/java/com/example/backend/user/service/UserService.java`
  - Demonstrates: @Transactional, @Auditable, SpecificationBuilder usage, CRUD operations, soft delete
- **Secondary**: `src/main/java/com/example/backend/audit/service/AuditLogService.java`
  - Demonstrates: read-only service, simple SpecificationBuilder

### Repositories
- **Primary**: `src/main/java/com/example/backend/user/repository/UserRepository.java`
  - Demonstrates: JpaSpecificationExecutor, custom query methods, @Query with JPQL
- **Secondary**: `src/main/java/com/example/backend/audit/repository/AuditLogRepository.java`
  - Demonstrates: delete methods, countBy methods

### Entities
- **Primary**: `src/main/java/com/example/backend/user/entity/User.java`
  - Demonstrates: BaseEntity extension, UserDetails implementation, soft delete, indexes, validation methods
- **Secondary**: `src/main/java/com/example/backend/product/entity/Product.java`
  - Demonstrates: simple entity, business logic methods, inventory management
- **Base**: `src/main/java/com/example/backend/common/BaseEntity.java`
  - Demonstrates: abstract base with audit fields, @MappedSuperclass, JPA auditing

### DTOs
- **Response DTO**: `src/main/java/com/example/backend/user/dto/UserDto.java`
  - Demonstrates: @Schema on class and fields, comprehensive documentation
- **Request DTO**: `src/main/java/com/example/backend/user/dto/CreateUserRequest.java`
  - Demonstrates: validation annotations, password handling
- **Update DTO**: `src/main/java/com/example/backend/user/dto/UpdateUserRequest.java`
  - Demonstrates: partial update DTO with all optional fields
- **Filter DTO**: `src/main/java/com/example/backend/user/dto/UserFilter.java`
  - Demonstrates: range fields (From/To), multi-select fields, DeletionStatus enum
- **Wrapper**: `src/main/java/com/example/backend/common/ApiResponse.java`
  - Demonstrates: generic response wrapper, static factory methods

### Mappers
- **Primary**: `src/main/java/com/example/backend/user/mapper/UserMapper.java`
  - Demonstrates: MapStruct interface, @Mapping annotations, field ignoring
- **Simple**: `src/main/java/com/example/backend/audit/mapper/AuditLogMapper.java`
  - Demonstrates: minimal mapper with single method

### AOP
- **Annotation**: `src/main/java/com/example/backend/audit/aop/Auditable.java`
  - Demonstrates: custom annotation with parameters
- **Aspect**: `src/main/java/com/example/backend/audit/aop/AuditAspect.java`
  - Demonstrates: @Around advice, async operations, exception handling, request context access

### Specifications
- **Builder**: `src/main/java/com/example/backend/common/specification/SpecificationBuilder.java`
  - Demonstrates: fluent API, method chaining, generic type handling
- **Utils**: `src/main/java/com/example/backend/common/utils/SpecificationUtils.java`
  - Demonstrates: reusable specification methods

### Exception Handling
- **Handler**: `src/main/java/com/example/backend/exception/GlobalExceptionHandler.java`
  - Demonstrates: @RestControllerAdvice, multiple exception handlers, validation error formatting
- **Custom**: `src/main/java/com/example/backend/exception/ResourceNotFoundException.java`
  - Demonstrates: custom exception with formatted message

### Security
- **Config**: `src/main/java/com/example/backend/config/SecurityConfig.java`
  - Demonstrates: SecurityFilterChain, AuthenticationProvider, CORS, CSRF
- **JWT Service**: `src/main/java/com/example/backend/security/JwtService.java`
  - Demonstrates: token generation, validation, claims extraction
- **Filter**: `src/main/java/com/example/backend/security/JwtAuthenticationFilter.java`
  - Demonstrates: OncePerRequestFilter, token extraction from cookies/headers

### Configuration
- **Async**: `src/main/java/com/example/backend/config/AsyncConfig.java`
  - Demonstrates: @EnableAsync, ThreadPoolTaskExecutor configuration
- **Jackson**: `src/main/java/com/example/backend/config/JacksonConfig.java`
  - Demonstrates: ObjectMapper bean, JavaTimeModule registration
- **Swagger**: `src/main/java/com/example/backend/config/SwaggerConfig.java`
  - Demonstrates: OpenAPI configuration, security schemes, API info

### Validation
- **Custom**: `src/main/java/com/example/backend/common/utils/PhoneValidator.java`
  - Demonstrates: validation utility with E.164 formatting
- **Annotation**: `src/main/java/com/example/backend/common/validation/ValidPhoneNumber.java`
  - Demonstrates: custom constraint annotation

### Database Migrations
- **Table Creation**: `src/main/resources/db/migration/V2__create_users_table.sql`
  - Demonstrates: Flyway migration, indexes, foreign keys, audit columns
- **Audit Table**: `src/main/resources/db/migration/V3__create_audit_logs_table.sql`
  - Demonstrates: TEXT columns, composite indexes

### Testing
- **Aspect Test**: `src/test/java/com/example/backend/audit/aop/AuditAspectTest.java`
  - Demonstrates: Mockito, @InjectMocks, SecurityContext mocking, async testing, ArgumentCaptor

### Utilities
- **Test Utils**: `src/main/java/com/example/backend/common/utils/TestUtils.java`
  - Demonstrates: ObjectMapper configuration, JSON serialization utilities

### Scheduled Tasks
- **Cleanup Service**: `src/main/java/com/example/backend/audit/service/AuditCleanupService.java`
  - Demonstrates: @Scheduled with cron, configurable retention, batch deletion

## Pattern Combinations

### CRUD Endpoint Pattern
Combine:
1. UserController (pagination, filtering)
2. UserService (@Transactional, @Auditable)
3. UserRepository (JpaSpecificationExecutor)
4. SpecificationBuilder (dynamic filtering)
5. UserMapper (entity ↔ DTO)
6. UserDto + CreateUserRequest + UpdateUserRequest + UserFilter

### Audit Trail Pattern
Combine:
1. Auditable annotation (mark methods)
2. AuditAspect (@Around advice)
3. AuditLog entity
4. AuditLogRepository
5. AsyncConfig (non-blocking saves)

### Soft Delete Pattern
Combine:
1. BaseEntity (deletedAt, deletedBy fields)
2. Entity methods: softDelete(), restore(), isDeleted()
3. Service @Auditable methods (DELETE_USER, RESTORE_USER)
4. UserFilter (DeletionStatus enum for filtering)

### Filtering Pattern
Combine:
1. Filter DTO with From/To fields
2. SpecificationBuilder in service
3. JpaSpecificationExecutor in repository
4. Controller builds filter from @RequestParam

### Security Pattern
Combine:
1. SecurityConfig (SecurityFilterChain)
2. JwtAuthenticationFilter (extract and validate token)
3. JwtService (generate and parse JWT)
4. @PreAuthorize in controllers
5. UserDetails implementation in User entity

## Domain Module Template

For new domain (e.g., Order, Invoice):
1. Create package: `com.example.backend.{domain}`
2. Entity: extends BaseEntity, @Table with indexes
3. Repository: extends JpaRepository, JpaSpecificationExecutor
4. Service: @Service, @Transactional, @Auditable on mutations
5. Mapper: @Mapper(componentModel = "spring")
6. DTOs: {Domain}Dto, Create{Domain}Request, Update{Domain}Request, {Domain}Filter
7. Controller: @RestController, /api/v1/{domains}, @PreAuthorize, @PageableDefault
8. Migration: V{N}__create_{domains}_table.sql
9. Tests: {Class}Test.java

## Key Relationships

### User Module
- User entity implements UserDetails (Spring Security integration)
- UserService implements UserDetailsService
- User has RefreshToken (one-to-many)
- User has Role (enum) and Permissions (collection)

### Audit Module
- AuditLog tracks all @Auditable operations
- AuditAspect intercepts and captures context
- AuditCleanupService runs monthly cleanup

### Product Module
- Product extends BaseEntity
- Product has quantity management methods
- ProductRepository has simple findBy queries

### Common Module
- BaseEntity: inherited by all entities
- ApiResponse: wraps all API responses
- SpecificationBuilder: used by all filtered services
- TestUtils: used in aspects and tests for JSON serialization
