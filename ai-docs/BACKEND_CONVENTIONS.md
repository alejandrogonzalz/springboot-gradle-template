# Backend Conventions

## Naming Conventions

### Classes
- Controllers: `{Entity}Controller` (e.g., `UserController`, `ProductController`)
- Services: `{Entity}Service` (e.g., `UserService`, `ProductService`)
- Repositories: `{Entity}Repository` (e.g., `UserRepository`, `ProductRepository`)
- Entities: `{Entity}` (e.g., `User`, `Product`, `AuditLog`)
- DTOs: `{Entity}Dto` (e.g., `UserDto`, `ProductDto`)
- Request DTOs: `{Action}{Entity}Request` (e.g., `CreateUserRequest`, `UpdateUserRequest`)
- Filter DTOs: `{Entity}Filter` (e.g., `UserFilter`, `ProductFilter`)
- Mappers: `{Entity}Mapper` (e.g., `UserMapper`, `ProductMapper`)
- Exceptions: `{Concern}Exception` (e.g., `ResourceNotFoundException`, `DuplicateResourceException`)
- Config classes: `{Concern}Config` (e.g., `SecurityConfig`, `DatabaseConfig`)
- Validators: `{Field}Validator` (e.g., `PhoneValidator`, `EmailValidator`)
- Enums: PascalCase (e.g., `UserRole`, `Permission`, `DeletionStatus`)

### Methods
- Repository find methods: `findBy{Field}` (e.g., `findByUsername`, `findByEmail`)
- Repository exists methods: `existsBy{Field}` (e.g., `existsByUsername`, `existsByEmail`)
- Repository count methods: `countBy{Field}` (e.g., `countByIsActive`, `countUsersByRole`)
- Repository delete methods: `deleteBy{Field}` (e.g., `deleteByCreatedAtBefore`, `deleteByUsername`)
- Service CRUD methods: `create{Entity}`, `get{Entity}By{Field}`, `getAll{Entity}s`, `update{Entity}`, `delete{Entity}`, `restore{Entity}`
- Controller methods: HTTP verb + noun (e.g., `createUser`, `getAllUsers`, `getUserById`, `updateUser`, `deleteUser`)
- Mapper methods: `toDto`, `toEntity`
- Specification building: `build{Entity}Specification`

### Fields
- Java fields: camelCase (e.g., `firstName`, `lastName`, `createdAt`)
- Database columns: snake_case (e.g., `first_name`, `last_name`, `created_at`)
- Boolean fields: `is{Condition}` or `has{Condition}` (e.g., `isActive`, `isDeleted`, `hasPermission`)
- Timestamp fields: `{action}At` (e.g., `createdAt`, `updatedAt`, `deletedAt`, `lastLoginDate`)
- Audit fields: `{action}By` (e.g., `createdBy`, `updatedBy`, `deletedBy`)
- Range filter fields: `{field}From`, `{field}To` (e.g., `createdAtFrom`, `createdAtTo`, `idFrom`, `idTo`)

### Constants
- Audit operations: UPPER_SNAKE_CASE (e.g., `CREATE_USER`, `UPDATE_USER`, `DELETE_USER`, `RESTORE_USER`)
- Enum values: UPPER_SNAKE_CASE (e.g., `PERMISSION_READ`, `PERMISSION_WRITE`, `ADMIN`, `USER`)

## Package Structure

```
com.example.backend/
├── config/                    # Configuration classes
├── security/                  # Security components (JWT, filters, entry points)
├── exception/                 # Global exception handler and custom exceptions
├── common/                    # Shared utilities
│   ├── BaseEntity.java
│   ├── ApiResponse.java
│   ├── specification/         # SpecificationBuilder and SpecificationUtils
│   ├── utils/                 # Utility classes (TestUtils, PhoneValidator)
│   └── validation/            # Custom validators
├── {domain}/                  # Feature modules (user, product, audit)
│   ├── aop/                   # AOP concerns (annotation + aspect)
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── mapper/
└── BackendApplication.java   # Main application class
```

## Endpoint Conventions

### Path Structure
- Base path: `/api/v1`
- Resource path: `/api/v1/{resources}` (plural, lowercase, hyphen-separated)
- Resource by ID: `/api/v1/{resources}/{id}`
- Sub-resources: `/api/v1/{resources}/{id}/{sub-resources}`
- Actions: `/api/v1/{resources}/{id}/{action}`

### Examples
- `POST /api/v1/users` - Create user
- `GET /api/v1/users` - List users (paginated)
- `GET /api/v1/users/{id}` - Get user by ID
- `GET /api/v1/users/username/{username}` - Get user by username
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user (soft delete)
- `POST /api/v1/users/{id}/restore` - Restore deleted user
- `GET /api/v1/users/statistics` - Get user statistics
- `GET /api/v1/audit-logs` - List audit logs (admin only)

### Query Parameters
- Pagination: `page`, `size`, `sort` (handled by Pageable)
- Filtering: query params matching filter DTO fields (e.g., `username`, `email`, `isActive`)
- Date ranges: `{field}From`, `{field}To` (e.g., `createdAtFrom`, `createdAtTo`)
- Multi-select: repeat param or comma-separated (e.g., `roles=ADMIN,USER` or `roles=ADMIN&roles=USER`)

### HTTP Methods
- `GET` - Read operations (idempotent)
- `POST` - Create operations (non-idempotent)
- `PUT` - Full update operations (idempotent)
- `PATCH` - Partial update operations (idempotent)
- `DELETE` - Delete operations (idempotent)

### Response Structure
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-12-31T23:00:00Z"
}
```

### Error Response Structure
```json
{
  "success": false,
  "message": "Error description",
  "data": {
    "field1": "Error for field1",
    "field2": "Error for field2"
  },
  "timestamp": "2024-12-31T23:00:00Z"
}
```

## Database Conventions

### Table Names
- snake_case, plural (e.g., `users`, `products`, `audit_logs`, `refresh_tokens`)

### Column Names
- snake_case (e.g., `first_name`, `last_name`, `created_at`, `is_active`)
- Primary keys: `id` (BIGINT, auto-increment)
- Foreign keys: `{referenced_table}_id` (e.g., `user_id`, `product_id`)
- Boolean flags: `is_{condition}` or `has_{condition}`
- Timestamps: `{action}_at` (e.g., `created_at`, `updated_at`, `deleted_at`)
- Audit columns: `{action}_by` (e.g., `created_by`, `updated_by`, `deleted_by`)

### Index Naming
- `idx_{table}_{column}` for single column index (e.g., `idx_user_username`, `idx_user_email`)
- `idx_{table}_{columns}` for composite index (e.g., `idx_audit_entity` for `entity_type, entity_id`)

### Constraint Naming
- Primary key: `pk_{table}`
- Foreign key: `fk_{table}_{referenced_table}`
- Unique: `uk_{table}_{column}`

## File Naming

### Java Files
- PascalCase matching class name (e.g., `UserController.java`, `UserService.java`)
- Test files: `{ClassName}Test.java` (e.g., `UserServiceTest.java`, `AuditAspectTest.java`)

### Migration Files
- `V{number}__{description}.sql` (e.g., `V1__create_products_table.sql`, `V2__create_users_table.sql`)
- Sequential numbering starting from 1
- Descriptive snake_case for description part

### Configuration Files
- `application.yml` - Main configuration
- `application-{profile}.yml` - Profile-specific configuration (e.g., `application-dev.yml`, `application-prod.yml`)

## Annotation Order

### Class-level
1. Framework annotations (@Entity, @Service, @RestController, @Configuration)
2. Component model (@Component, @Repository)
3. Cross-cutting concerns (@Transactional, @Slf4j, @RequiredArgsConstructor)
4. Lombok annotations (@Getter, @Setter, @Data, @Builder, @SuperBuilder, @NoArgsConstructor, @AllArgsConstructor)
5. Documentation (@Tag, @Schema)
6. Mapping (@RequestMapping, @Table)

### Field-level
1. Validation (@NotNull, @NotBlank, @Size, @Email, @Pattern)
2. JPA (@Column, @ManyToOne, @OneToMany, @JoinColumn)
3. Documentation (@Schema, @Parameter)
4. JSON serialization (@JsonIgnore, @JsonProperty)

### Method-level
1. HTTP mapping (@GetMapping, @PostMapping, @PutMapping, @DeleteMapping)
2. Security (@PreAuthorize)
3. Transactions (@Transactional)
4. Async (@Async)
5. Scheduled (@Scheduled)
6. Auditing (@Auditable)
7. Documentation (@Operation, @ApiResponse)
8. Validation (@Valid)

## Lombok Conventions
- Use `@Data` for simple DTOs (includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor)
- Use `@Getter` + `@Setter` separately for entities (more control)
- Use `@SuperBuilder` for entities extending BaseEntity
- Use `@Builder` for DTOs without inheritance
- Use `@RequiredArgsConstructor` for constructor injection
- Use `@NoArgsConstructor` for JPA entities (required by spec)
- Use `@AllArgsConstructor` with @Builder for DTOs
- Use `@Slf4j` for logging

## Import Organization
1. Java standard library (java.*)
2. Jakarta (jakarta.*)
3. Spring (org.springframework.*)
4. Third-party libraries (alphabetical)
5. Project imports (com.example.backend.*)
6. Static imports (at end)
