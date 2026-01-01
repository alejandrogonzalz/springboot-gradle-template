# Backend Patterns

## Controllers
- Must use `@RestController` annotation
- Must use `@RequestMapping` with versioned path `/api/v1/{resource}`
- Must use `@RequiredArgsConstructor` for dependency injection
- Must use `@Slf4j` for logging
- Must use `@Tag` for Swagger documentation
- Must return `ResponseEntity<ApiResponse<T>>` for all endpoints
- Must use `@Valid` on request bodies requiring validation
- Must use `@PathVariable` for resource IDs in path
- Must use `@RequestParam(required = false)` for optional query parameters
- Must use `@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)` for paginated endpoints
- Must use `@Parameter` annotations for Swagger documentation on all parameters
- Must use `@PreAuthorize` for method-level security
- Must log requests with `log.info` including key parameters
- Must call service layer methods (never call repositories directly)
- Must use `ApiResponse.success(data)` or `ApiResponse.success(data, message)` for successful responses
- Must handle filter building by creating Filter DTOs from individual query parameters

## Services
- Must use `@Service` annotation
- Must use `@Transactional(readOnly = true)` at class level
- Must use `@Transactional` on write methods
- Must use `@RequiredArgsConstructor` for dependency injection
- Must use `@Slf4j` for logging
- Must inject repositories and mappers via constructor
- Must use mappers to convert entities to DTOs (never return entities directly)
- Must use `@Auditable` annotation on create/update/delete operations
- Must throw `ResourceNotFoundException` when entity not found
- Must throw `DuplicateResourceException` when unique constraint violated
- Must throw `IllegalStateException` for business rule violations
- Must use `SpecificationBuilder` for building JPA Specifications from filters
- Must extract specification building logic into private methods named `build{Entity}Specification`
- Must log operations with structured messages including entity IDs
- Must never expose password hashes in DTOs

## Repositories
- Must extend `JpaRepository<Entity, Long>`
- Must extend `JpaSpecificationExecutor<Entity>` if filtering is needed
- Must use `Optional<T>` for findBy methods that may return null
- Must use `boolean` for exists methods
- Must use `long` for count methods
- Must use `@Query` with JPQL for complex queries
- Must document all custom query methods with Javadoc
- Must name query methods following Spring Data conventions: `findBy`, `existsBy`, `countBy`, `deleteBy`

## Entities
- Must extend `BaseEntity` abstract class
- Must use `@Entity` annotation
- Must use `@Table` with name and indexes
- Must use `@Getter` and `@Setter` from Lombok
- Must use `@SuperBuilder` for builder pattern with inheritance
- Must use `@NoArgsConstructor` for JPA
- Must use `@Column` annotations with name, nullable, length constraints
- Must use snake_case for database column names
- Must use camelCase for Java field names
- Must implement soft delete methods if applicable: `softDelete(String deletedBy)`, `restore()`, `isDeleted()`
- Must include indexes on frequently queried columns
- Must never expose passwords or sensitive data in toString/equals/hashCode

## DTOs
- Must use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` annotations
- Must use `@Schema` for Swagger documentation
- Must use `@Schema` on all fields with description and example
- Must use `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Pattern` for validation on request DTOs
- Must suffix response DTOs with `Dto`
- Must suffix request DTOs with `Request`
- Must suffix filter DTOs with `Filter`
- Must use `Instant` for timestamps (not LocalDateTime or Date)
- Must use enums for allowableValues in `@Schema` annotations
- Must never include password fields in response DTOs
- Must never include internal audit fields (createdBy, updatedBy) in request DTOs

## Filter DTOs
- Must use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Must use `@Schema` annotation
- Must provide `From` and `To` fields for range queries (e.g., `createdAtFrom`, `createdAtTo`)
- Must provide singular fields for exact matches
- Must provide plural fields (List) for multi-select filters
- Must use `Instant` for date/time range fields
- Must make all fields optional (no @NotNull)

## Mappers
- Must use `@Mapper(componentModel = "spring")` for MapStruct
- Must be interfaces (not classes)
- Must suffix with `Mapper`
- Must provide `toDto(Entity entity)` method
- Must provide `toEntity(RequestDto dto)` method if needed
- Must use `@Mapping(target = "field", ignore = true)` for fields not mapped from DTO
- Must use `@Mapping(target = "field", source = "sourceField")` for field name differences
- Must never map password fields directly

## Security
- Must use `@EnableMethodSecurity` in SecurityConfig
- Must use `@PreAuthorize("hasRole('ADMIN')")` for admin-only endpoints
- Must use `@PreAuthorize("hasRole('ADMIN') or hasAuthority('PERMISSION_NAME')")` for permission-based access
- Must use `SecurityContextHolder.getContext().getAuthentication()` to get current user
- Must validate user permissions in service layer when business logic requires it
- Must hash passwords with `PasswordEncoder` before saving
- Must never log or expose passwords or tokens
- Must use JWT tokens for authentication
- Must use refresh tokens stored in HTTP-only cookies

## Validation
- Must use Jakarta validation annotations (@NotNull, @NotBlank, @Size, @Email, @Pattern)
- Must validate at DTO level (not entity level)
- Must create custom validators for complex validation (e.g., @ValidPhoneNumber)
- Must use `@Valid` in controller methods to trigger validation
- Must return structured validation errors via GlobalExceptionHandler

## Exception Handling
- Must use `@RestControllerAdvice` for global exception handler
- Must handle all standard exceptions: ResourceNotFoundException, DuplicateResourceException, IllegalStateException, IllegalArgumentException
- Must handle validation exceptions: MethodArgumentNotValidException, ConstraintViolationException
- Must handle security exceptions: AccessDeniedException, BadCredentialsException, UnauthorizedException
- Must return `ApiResponse<T>` with error details
- Must log errors with appropriate level (warn for client errors, error for server errors)
- Must never expose stack traces in production responses
- Must return appropriate HTTP status codes: 200 (OK), 201 (Created), 400 (Bad Request), 401 (Unauthorized), 403 (Forbidden), 404 (Not Found), 409 (Conflict), 500 (Internal Server Error)

## Audit Logging
- Must use `@Auditable(operation = "OPERATION_NAME", entityType = "EntityName")` on service methods
- Must use operation names in UPPER_SNAKE_CASE
- Must capture data by default (`captureData = true`)
- Must provide meaningful descriptions
- Must track: username, operation, entityType, entityId, ipAddress, requestUri, httpMethod, success, changes
- Must save audit logs asynchronously
- Must never fail main operation if audit logging fails

## Database Migrations
- Must use Flyway for schema migrations
- Must name migrations `V{number}__{description}.sql`
- Must use snake_case for table and column names
- Must create indexes on foreign keys and frequently queried columns
- Must use `created_at`, `updated_at`, `created_by`, `updated_by` for audit fields
- Must use `deleted_at`, `deleted_by` for soft delete
- Must use `is_active` for soft delete flag
- Must never modify existing migration files
- Must create new migration for schema changes

## Pagination
- Must use `Page<T>` return type for paginated responses
- Must use `Pageable` parameter in repository and service methods
- Must use `@PageableDefault` in controllers with sensible defaults (size = 20)
- Must default sort by `createdAt DESC` for time-based entities
- Must use `SpecificationBuilder` for dynamic filtering with pagination

## Specifications (JPA Criteria)
- Must use `SpecificationBuilder<Entity>` for building dynamic queries
- Must chain filter methods fluently: `.equals()`, `.contains()`, `.between()`, `.in()`, `.joinIn()`
- Must call `.build()` at the end to produce `Specification<Entity>`
- Must delegate to `SpecificationUtils` for core specification logic
- Must handle null/empty values gracefully (skip filter if null)
- Must use `contains()` for case-insensitive partial string matching
- Must use `between()` for range queries (supports null bounds)
- Must use `joinIn()` for filtering on joined collections

## Configuration
- Must separate concerns into dedicated config classes: SecurityConfig, DatabaseConfig, SwaggerConfig, AsyncConfig, CacheConfig
- Must use `@Configuration` annotation
- Must use `@EnableMethodSecurity`, `@EnableJpaAuditing`, `@EnableAsync`, `@EnableCaching`, `@EnableScheduling` as needed
- Must externalize sensitive configuration to application.yml
- Must provide meaningful defaults for optional configuration

## Testing
- Must use `@ExtendWith(MockitoExtension.class)` for unit tests
- Must use `@Mock` for dependencies
- Must use `@InjectMocks` for class under test
- Must use `@BeforeEach` for setup
- Must use `@DisplayName` for readable test names
- Must use AssertJ for assertions
- Must mock SecurityContext for authentication tests
- Must verify audit logging in tests
- Must test successful and failure scenarios
- Must never use real databases in unit tests

## Logging
- Must use `@Slf4j` annotation
- Must use appropriate log levels: debug (detailed flow), info (major operations), warn (recoverable issues), error (failures)
- Must include entity IDs and key parameters in log messages
- Must use structured logging: `log.info("Message with {}", param)`
- Must never log sensitive data (passwords, tokens, PII)
- Must log exceptions with stack trace: `log.error("Message", exception)`

## Async Operations
- Must use `@Async` annotation for non-blocking operations
- Must configure AsyncConfig with thread pool
- Must never block on async operations in request path
- Must handle exceptions in async methods (never propagate to caller)
- Must use async for audit logging, email sending, long-running tasks

## Scheduled Tasks
- Must use `@Scheduled` annotation with cron expression
- Must enable with `@EnableScheduling` in main application class
- Must externalize cron expressions to application.yml
- Must use `@Transactional` for database operations in scheduled tasks
- Must log start and completion of scheduled tasks
- Must handle exceptions gracefully (don't break scheduler)

## Package Structure
- Must organize by feature: `{domain}/controller`, `{domain}/service`, `{domain}/repository`, `{domain}/entity`, `{domain}/dto`, `{domain}/mapper`
- Must place common utilities in `common/` package
- Must place cross-cutting concerns (security, config, exception) in dedicated packages
- Must place AOP concerns together: `{domain}/aop/`
- Must place validation in `common/validation/`
- Must never mix concerns across packages
