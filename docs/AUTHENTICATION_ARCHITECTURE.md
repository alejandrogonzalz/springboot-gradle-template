# 🔐 Authentication & Authorization Architecture

## 📋 Table of Contents
- [Overview](#overview)
- [Architecture Diagram](#architecture-diagram)
- [Database Schema](#database-schema)
- [JWT Tokens](#jwt-tokens)
- [Spring Security Configuration](#spring-security-configuration)
- [Authentication Flow](#authentication-flow)
- [Authorization Model](#authorization-model)
- [API Endpoints](#api-endpoints)
- [Testing Authentication](#testing-authentication)

---

## Overview

This application uses a **JWT-based stateless authentication** system with **role-based access control (RBAC)** enhanced by **custom per-user permissions** (hybrid approach).

### Key Features
- ✅ JWT access tokens (24 hours)
- ✅ JWT refresh tokens (7 days)
- ✅ BCrypt password hashing
- ✅ Role-based permissions (ADMIN, USER, GUEST)
- ✅ Custom per-user permissions
- ✅ Spring Security method-level authorization
- ✅ Stateless sessions (no server-side session storage)

---

## Architecture Diagram

```
┌─────────────┐
│   Client    │
│ (Postman/   │
│  Browser)   │
└──────┬──────┘
       │
       │ 1. POST /api/v1/auth/login
       │    { username, password }
       ▼
┌─────────────────────────────────────────────────┐
│          Spring Security Filter Chain           │
│                                                  │
│  ┌────────────────────────────────────────┐    │
│  │  JwtAuthenticationFilter               │    │
│  │  - Extracts JWT from Authorization     │    │
│  │  - Validates token signature           │    │
│  │  - Loads UserDetails                   │    │
│  │  - Sets SecurityContext                │    │
│  └────────────────────────────────────────┘    │
└─────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────┐
│        AuthenticationManager                     │
│  - Authenticates username/password              │
│  - Uses DaoAuthenticationProvider               │
└─────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────┐
│           UserDetailsService                     │
│  - Loads user from database                     │
│  - Returns User (implements UserDetails)        │
└─────────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────────┐
│              JwtService                          │
│  - Generates access token (24h)                 │
│  - Generates refresh token (7d)                 │
│  - Signs with HMAC-SHA256                       │
└─────────────────────────────────────────────────┘
       │
       │ 2. Response with tokens
       ▼
┌─────────────────────────────────────────────────┐
│  {                                               │
│    "accessToken": "eyJhbGc...",                 │
│    "refreshToken": "eyJhbGc...",                │
│    "tokenType": "Bearer",                       │
│    "expiresIn": 86400000                        │
│  }                                               │
└─────────────────────────────────────────────────┘
       │
       │ 3. Subsequent requests
       │    Authorization: Bearer <accessToken>
       ▼
┌─────────────────────────────────────────────────┐
│         Protected Endpoints                      │
│  - @PreAuthorize checks authorities             │
│  - SecurityContext provides authentication      │
└─────────────────────────────────────────────────┘
```

---

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role ENUM('ADMIN', 'USER', 'GUEST') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_date TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_active (is_active)
);
```

### User Permissions Table (Hybrid Feature)

```sql
CREATE TABLE user_permissions (
    user_id BIGINT NOT NULL,
    permission ENUM('READ', 'CREATE', 'UPDATE', 'DELETE', 'ADMIN', 'MANAGE_USERS', 'VIEW_AUDIT_LOGS') NOT NULL,
    PRIMARY KEY (user_id, permission),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_permissions_user_id (user_id)
);
```

**Purpose**: Stores additional custom permissions for specific users beyond their role's default permissions.

**Example Use Case**:
- User "john" has role USER (READ, CREATE, UPDATE)
- Grant john DELETE permission: `INSERT INTO user_permissions VALUES (5, 'DELETE')`
- Now john has: READ, CREATE, UPDATE, DELETE

---

## JWT Tokens

### Token Structure

JWT tokens consist of three parts separated by dots (`.`):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VybmFtZSIsImlhdCI6MTYxNjIzOTAyMiwiZXhwIjoxNjE2MzI1NDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
│──────────── Header ────────────│───────────────── Payload ──────────────│───────── Signature ────────│
```

### Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload (Claims)
```json
{
  "sub": "username",           // Subject (username)
  "iat": 1616239022,           // Issued at (timestamp)
  "exp": 1616325422            // Expiration (timestamp)
}
```

### Signature
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

### Token Types

| Token Type | Expiration | Purpose | Storage |
|------------|-----------|---------|---------|
| **Access Token** | 24 hours | API authentication | Memory/localStorage |
| **Refresh Token** | 7 days | Renew access token | HttpOnly cookie (recommended) |

### Configuration

Located in `application.yml`:

```yaml
jwt:
  secret: ${JWT_SECRET:defaultSecretKeyForDevelopment}
  expiration: 86400000      # 24 hours in milliseconds
  refresh-expiration: 604800000  # 7 days in milliseconds
```

**Security Note**: Always set `JWT_SECRET` environment variable in production!

```bash
export JWT_SECRET="your-super-secret-key-min-32-chars-long"
```

---

## Spring Security Configuration

### SecurityConfig Class

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)  // Disable CSRF for stateless JWT
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/actuator/health")
                    .permitAll()
                // All other endpoints require authentication
                .anyRequest()
                    .authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### Key Components

#### 1. JwtAuthenticationFilter

**Purpose**: Intercepts every request to validate JWT tokens.

**Flow**:
```
Request → Extract JWT from "Authorization: Bearer <token>"
       → Validate token signature
       → Extract username from token
       → Load UserDetails from database
       → Verify token is valid for this user
       → Set SecurityContextHolder with authentication
       → Continue filter chain
```

#### 2. DaoAuthenticationProvider

**Purpose**: Authenticates users against the database.

```java
@Bean
public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
}
```

#### 3. BCryptPasswordEncoder

**Purpose**: Hashes passwords with BCrypt (strength 10).

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

---

## Authentication Flow

### 1. User Registration

```
POST /api/v1/auth/register
{
  "username": "johndoe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com"
}

Flow:
1. Validate input (username unique, email unique)
2. Hash password with BCrypt
3. Set default role: USER
4. Save user to database
5. Generate JWT tokens
6. Return tokens
```

### 2. User Login

```
POST /api/v1/auth/login
{
  "username": "johndoe",
  "password": "SecurePass123!"
}

Flow:
1. AuthenticationManager.authenticate(username, password)
2. DaoAuthenticationProvider validates credentials
3. Load User from database
4. BCrypt verifies password hash
5. Generate access token (24h)
6. Generate refresh token (7d)
7. Update last_login_date
8. Return tokens
```

**Response**:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  }
}
```

### 3. Token Refresh

```
POST /api/v1/auth/refresh
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Flow:
1. Validate refresh token signature
2. Check refresh token not expired
3. Extract username from token
4. Load user from database
5. Verify user is active
6. Generate new access token (24h)
7. Return same refresh token + new access token
```

### 4. Protected Endpoint Access

```
GET /api/v1/users
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Flow:
1. JwtAuthenticationFilter extracts token
2. Validate token signature with secret key
3. Check token not expired
4. Extract username from token
5. Load User from database (with roles and permissions)
6. Set SecurityContext with authentication
7. @PreAuthorize checks if user has required authority
8. If authorized, execute method
9. Return response
```

---

## Authorization Model

### Role Hierarchy

```
ADMIN > USER > GUEST
```

### Role-Based Permissions

| Role | Permissions |
|------|-------------|
| **ADMIN** | READ, CREATE, UPDATE, DELETE, ADMIN, MANAGE_USERS, VIEW_AUDIT_LOGS |
| **USER** | READ, CREATE, UPDATE |
| **GUEST** | READ |

### Permission Authority Format

Spring Security uses the following authority format:

| Type | Format | Example |
|------|--------|---------|
| **Role** | `ROLE_<NAME>` | `ROLE_ADMIN`, `ROLE_USER` |
| **Permission** | `PERM_<NAME>` | `PERM_READ`, `PERM_CREATE` |

### User Authority Composition

When Spring Security calls `user.getAuthorities()`, it returns:

```java
// Example for USER with additional DELETE permission
[
  "ROLE_USER",           // From role
  "PERM_READ",          // From role default permissions
  "PERM_CREATE",        // From role default permissions
  "PERM_UPDATE",        // From role default permissions
  "PERM_DELETE"         // From additionalPermissions (user_permissions table)
]
```

### Implementation

#### UserRole Enum
```java
public enum UserRole {
    ADMIN(Set.of(
        Permission.READ, Permission.CREATE, Permission.UPDATE,
        Permission.DELETE, Permission.ADMIN,
        Permission.MANAGE_USERS, Permission.VIEW_AUDIT_LOGS
    )),
    USER(Set.of(
        Permission.READ, Permission.CREATE, Permission.UPDATE
    )),
    GUEST(Set.of(
        Permission.READ
    ));

    public String getRoleName() {
        return "ROLE_" + this.name();
    }

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(getRoleName()));
        authorities.addAll(permissions.stream()
            .map(Permission::toAuthority)
            .collect(Collectors.toSet()));
        return authorities;
    }
}
```

#### Permission Enum
```java
public enum Permission {
    READ("PERM_READ"),
    CREATE("PERM_CREATE"),
    UPDATE("PERM_UPDATE"),
    DELETE("PERM_DELETE"),
    ADMIN("PERM_ADMIN"),
    MANAGE_USERS("PERM_MANAGE_USERS"),
    VIEW_AUDIT_LOGS("PERM_VIEW_AUDIT_LOGS");

    private final String authority;

    public GrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority(authority);
    }
}
```

#### User Entity
```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();

    // Add role and its default permissions
    authorities.addAll(role.getAuthorities());

    // Add additional custom permissions (from user_permissions table)
    authorities.addAll(
        additionalPermissions.stream()
            .map(Permission::toAuthority)
            .collect(Collectors.toSet()));

    return authorities;
}
```

### Method Security (@PreAuthorize)

#### Role-Based
```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public List<UserDto> getAllUsers() {
    // Only users with ROLE_ADMIN can access
}
```

#### Permission-Based
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasAuthority('PERM_DELETE')")
public void deleteProduct(@PathVariable Long id) {
    // Only users with PERM_DELETE can access
    // Works for: ADMIN (has by default) or USER with custom DELETE permission
}
```

#### Combined Authorization
```java
@PutMapping("/{id}")
@PreAuthorize("hasAuthority('PERM_UPDATE') or #id == authentication.principal.id")
public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto dto) {
    // User can update if:
    // 1. They have PERM_UPDATE permission, OR
    // 2. They are updating their own profile
}
```

#### Multiple Authorities
```java
@DeleteMapping("/{id}")
@PreAuthorize("hasAuthority('PERM_DELETE') or hasRole('ADMIN')")
public void deleteUser(@PathVariable Long id) {
    // User can delete if:
    // 1. They have PERM_DELETE permission, OR
    // 2. They are an ADMIN
}
```

---

## API Endpoints

### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | Login and get tokens |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| GET | `/actuator/health` | Health check |
| GET | `/swagger-ui.html` | API documentation |

### Protected Endpoints (JWT Required)

#### User Management

| Method | Endpoint | Required Permission | Description |
|--------|----------|-------------------|-------------|
| GET | `/api/v1/users` | `PERM_READ` | List all users |
| GET | `/api/v1/users/{id}` | `PERM_READ` | Get user by ID |
| GET | `/api/v1/users/username/{username}` | `PERM_READ` | Get user by username |
| PUT | `/api/v1/users/{id}` | `PERM_UPDATE` or own profile | Update user |
| DELETE | `/api/v1/users/{id}` | `PERM_DELETE` or `PERM_ADMIN` | Delete user |

#### Product Management

| Method | Endpoint | Required Permission | Description |
|--------|----------|-------------------|-------------|
| GET | `/api/v1/products` | `PERM_READ` | List all products |
| GET | `/api/v1/products/{id}` | `PERM_READ` | Get product by ID |
| POST | `/api/v1/products` | `PERM_CREATE` | Create product |
| PUT | `/api/v1/products/{id}` | `PERM_UPDATE` | Update product |
| DELETE | `/api/v1/products/{id}` | `PERM_DELETE` | Delete product |

---

## Testing Authentication

### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!"
  }'
```

**Save the `accessToken` from response!**

### 3. Access Protected Endpoint

```bash
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Refresh Token

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

---

## Security Best Practices

### ✅ DO

1. **Store JWT secret in environment variable**
   ```bash
   export JWT_SECRET="your-32-char-minimum-secret-key"
   ```

2. **Use HTTPS in production**
   - Prevents token interception

3. **Store refresh tokens in HttpOnly cookies**
   - Prevents XSS attacks

4. **Implement token blacklisting for logout**
   - Use Redis to store invalidated tokens

5. **Rotate JWT secrets periodically**
   - Implement secret rotation strategy

6. **Use strong password requirements**
   - Minimum 8 characters
   - Mix of uppercase, lowercase, numbers, special chars

7. **Rate limit authentication endpoints**
   - Prevent brute force attacks

8. **Log authentication events**
   - Track failed login attempts
   - Monitor suspicious activity

### ❌ DON'T

1. **Don't store sensitive data in JWT payload**
   - JWT is base64 encoded, not encrypted
   - Anyone can decode and read the payload

2. **Don't use weak JWT secrets**
   - Minimum 32 characters
   - Use cryptographically secure random generation

3. **Don't store tokens in localStorage if XSS is a concern**
   - Use HttpOnly cookies instead

4. **Don't set long expiration times**
   - Access tokens: max 24 hours
   - Refresh tokens: max 7 days

5. **Don't ignore token validation failures**
   - Always return 401 Unauthorized for invalid tokens

---

## Troubleshooting

### Issue: "401 Unauthorized" on protected endpoints

**Causes**:
- Missing or invalid JWT token
- Token expired
- Invalid signature (wrong secret key)
- User no longer exists or is inactive

**Solution**:
1. Check `Authorization` header format: `Bearer <token>`
2. Verify token not expired
3. Login again to get new token
4. Check JWT_SECRET matches between environments

### Issue: "403 Forbidden" on protected endpoints

**Causes**:
- User authenticated but lacks required permission/role
- @PreAuthorize condition not met

**Solution**:
1. Check user's roles and permissions
2. Verify @PreAuthorize annotation matches user's authorities
3. Grant additional permissions via `user_permissions` table if needed

### Issue: Password authentication fails

**Causes**:
- Wrong password
- Password not BCrypt hashed in database
- BCrypt rounds mismatch

**Solution**:
1. Verify password is correct
2. Check `password_hash` column starts with `$2a$10$`
3. Re-hash password if needed

---

## Conclusion

This authentication system provides:
- ✅ Stateless JWT-based authentication
- ✅ Role-based access control (RBAC)
- ✅ Custom per-user permissions (hybrid approach)
- ✅ Industry-standard Spring Security patterns
- ✅ Flexible and scalable architecture

**Hybrid Approach Note**: While this uses a hybrid approach with custom per-user permissions, the core authentication and authorization mechanisms follow pure Spring Security patterns. The `user_permissions` table is an extension that provides additional flexibility without compromising the security model.
