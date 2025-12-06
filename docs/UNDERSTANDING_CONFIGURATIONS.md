# Understanding Application Configurations

This document explains the key configuration files and how caching works in this application.

## 📋 Table of Contents
1. [Logback Logging Configuration](#logback-logging)
2. [Application.yml Configuration](#applicationyml)
3. [Cache Configuration & Redis](#cache-and-redis)
4. [Production Setup (AWS ElastiCache)](#production-aws-elasticache)

---

## 1. Logback Logging Configuration

### File: `src/main/resources/logback-spring.xml`

Logback is the logging framework used by Spring Boot. This file controls WHERE and HOW logs are written.

### Key Components:

#### **A. Console Appender**
```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```
- **Purpose**: Writes logs to console (stdout)
- **Pattern breakdown**:
  - `%d{...}` - Timestamp
  - `[%thread]` - Thread name
  - `%-5level` - Log level (INFO, DEBUG, ERROR, etc.)
  - `%logger{36}` - Logger name (class name, truncated to 36 chars)
  - `%msg` - The actual log message
  - `%n` - New line

**Example Output**:
```
2024-12-06 10:30:45.123 [http-nio-8080-exec-1] INFO  c.e.backend.controller.ProductController - GET /api/v1/products/1
```

#### **B. File Appender** (Rolling)
```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/application.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxFileSize>10MB</maxFileSize>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
</appender>
```
- **Purpose**: Writes logs to file with automatic rotation
- **Rolling Strategy**:
  - Creates new file daily
  - When file exceeds 10MB, creates new file with index (.1, .2, etc.)
  - Keeps last 30 days of logs
  - Old logs are automatically deleted

**Generated Files**:
```
logs/
├── application.log              (current)
├── application-2024-12-05.1.log (yesterday, first rotation)
├── application-2024-12-05.2.log (yesterday, second rotation)
└── application-2024-12-04.1.log (2 days ago)
```

#### **C. JSON Appender** (Production)
```xml
<appender name="JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"application":"${application}"}</customFields>
    </encoder>
</appender>
```
- **Purpose**: Writes logs in JSON format for log aggregation systems
- **Use Case**: CloudWatch, Splunk, ELK Stack, Datadog
- **Benefits**: Structured logs are easily searchable and parseable

**Example JSON Output**:
```json
{
  "@timestamp": "2024-12-06T10:30:45.123Z",
  "level": "INFO",
  "thread": "http-nio-8080-exec-1",
  "logger": "com.example.backend.controller.ProductController",
  "message": "GET /api/v1/products/1",
  "application": "backend-application"
}
```

#### **D. Log Levels**
```xml
<logger name="com.example.backend" level="DEBUG"/>
<logger name="org.springframework.web" level="INFO"/>
<logger name="org.hibernate" level="INFO"/>

<root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
    <appender-ref ref="JSON"/>
</root>
```

**Log Level Hierarchy** (least to most verbose):
- **ERROR** - Only errors
- **WARN** - Warnings + errors
- **INFO** - Informational + warnings + errors
- **DEBUG** - Debug info + all above
- **TRACE** - Everything (very verbose)

**What this configuration means**:
- Your application code (`com.example.backend`): DEBUG level (very detailed)
- Spring framework: INFO level (only important info)
- Hibernate: INFO level (no SQL queries unless needed)
- Everything else: INFO level (default)

---

## 2. Application.yml Configuration

### File: `src/main/resources/application.yml`

This is the main configuration file for your Spring Boot application. Let me break down each section:

### **A. Database Configuration**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/backend_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: backend_user
    password: backend_password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10      # Max connections in pool
      minimum-idle: 5             # Min idle connections kept ready
      connection-timeout: 30000   # Max wait time for connection (30s)
      idle-timeout: 600000        # How long idle connections stay alive (10min)
      max-lifetime: 1800000       # Max connection lifetime (30min)
```

**HikariCP Explanation**:
- **Connection Pool**: Instead of creating a new database connection for each request (expensive!), HikariCP maintains a pool of reusable connections
- **maximum-pool-size: 10**: Max 10 simultaneous DB connections
- **minimum-idle: 5**: Always keep 5 connections ready
- **Lifecycle**: Connections are recycled after 30 minutes to prevent stale connections

### **B. JPA/Hibernate Configuration**

```yaml
jpa:
  hibernate:
    ddl-auto: validate  # IMPORTANT: Only validates schema, doesn't modify DB
  show-sql: false       # Set to true to see SQL queries in logs
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQLDialect  # MySQL-specific SQL
      format_sql: true        # Pretty-print SQL
      jdbc:
        batch_size: 20        # Batch 20 inserts/updates together
      order_inserts: true     # Order inserts for better batching
      order_updates: true     # Order updates for better batching
  open-in-view: false         # Best practice: prevent lazy loading issues
```

**ddl-auto options**:
- `validate` - ✅ **Production**: Only checks if DB schema matches entities
- `update` - ⚠️ Dev only: Updates schema automatically (can cause data loss)
- `create` - ❌ Never in prod: Drops and recreates tables
- `create-drop` - ❌ Testing only: Drops tables on shutdown

### **C. Flyway Configuration**

```yaml
flyway:
  enabled: true
  baseline-on-migrate: true    # Creates baseline for existing databases
  locations: classpath:db/migration
  schemas: backend_db
```

**How Flyway Works**:
1. Looks in `src/main/resources/db/migration/`
2. Runs SQL files in version order (V1__, V2__, V3__, etc.)
3. Tracks executed migrations in `flyway_schema_history` table
4. Never runs the same migration twice
5. Ensures database consistency across environments

### **D. Jackson (JSON) Configuration**

```yaml
jackson:
  serialization:
    write-dates-as-timestamps: false  # Use ISO-8601 format, not epoch
    indent-output: true                # Pretty-print JSON (disable in prod)
  deserialization:
    fail-on-unknown-properties: false  # Ignore extra fields in JSON
  default-property-inclusion: non_null # Don't include null fields
```

**Effect on API responses**:
```json
{
  "id": 1,
  "name": "Laptop",
  "createdAt": "2024-12-06T10:30:45Z",  // ISO-8601, not 1733482245000
  "description": null  // This field will be OMITTED due to non_null
}
```

### **E. Server Configuration**

```yaml
server:
  port: 8080
  error:
    include-message: always           # Always show error messages
    include-binding-errors: always    # Show validation errors
    include-stacktrace: on_param      # Show stack trace if ?trace=true
    include-exception: false          # Don't expose exception class names
  compression:
    enabled: true                     # Gzip compression for responses
    min-response-size: 1024           # Only compress responses > 1KB
```

### **F. Actuator (Health Checks)**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized  # Hide details from public
```

**Available Endpoints**:
- `/actuator/health` - Is the app running?
- `/actuator/metrics` - JVM, HTTP, database metrics
- `/actuator/prometheus` - Metrics in Prometheus format

---

## 3. Cache Configuration & Redis

### ⚠️ **IMPORTANT: Currently NOT using Redis!**

The current `CacheConfig.java` uses **in-memory caching** with `ConcurrentMapCacheManager`:

```java
@Bean
public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager(
            PRODUCTS_CACHE,
            PRODUCT_BY_ID_CACHE
    );
}
```

**Problem with In-Memory Cache**:
- ❌ Cache is lost on application restart
- ❌ Each application instance has its own cache
- ❌ No cache sharing between multiple servers
- ❌ Not suitable for production with multiple instances

### ✅ **Let's Fix This: Connect to Redis**

I'll now update the configuration to properly use Redis.

---

## 4. Production Setup (AWS ElastiCache)

I'll create configurations for:
1. **Local development** (Docker Compose Redis)
2. **Production** (AWS ElastiCache)
3. **Configuration profiles** for easy switching
