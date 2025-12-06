# Complete Configuration Guide

## 📚 Table of Contents
1. [Logback Logging Deep Dive](#1-logback-logging-deep-dive)
2. [Application.yml Deep Dive](#2-applicationyml-deep-dive)
3. [Redis Cache Configuration](#3-redis-cache-configuration)
4. [AWS Production Setup](#4-aws-production-setup)
5. [Configuration Profiles](#5-configuration-profiles)

---

## 1. Logback Logging Deep Dive

### File: `src/main/resources/logback-spring.xml`

### What is Logback?
Logback is the **successor to Log4j** and the default logging framework in Spring Boot. It determines:
- **WHERE** logs go (console, files, external systems)
- **WHAT FORMAT** they're in (plain text, JSON)
- **HOW MUCH** detail to log (DEBUG, INFO, ERROR)

---

### Appenders: Where Logs Go

#### **A. CONSOLE Appender** 📺

```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>
```

**What it does**: Prints logs to your terminal/console

**Pattern Breakdown**:
```
%d{yyyy-MM-dd HH:mm:ss.SSS} → 2024-12-06 10:30:45.123
[%thread]                    → [http-nio-8080-exec-1]
%-5level                     → INFO  (padded to 5 chars)
%logger{36}                  → c.e.backend.controller.ProductController
%msg                         → GET /api/v1/products/1
%n                           → New line
```

**Example Output**:
```
2024-12-06 10:30:45.123 [http-nio-8080-exec-1] INFO  c.e.backend.controller.ProductController - GET /api/v1/products/1
2024-12-06 10:30:45.234 [http-nio-8080-exec-1] DEBUG c.e.backend.service.ProductService - Fetching product with id: 1
2024-12-06 10:30:45.456 [http-nio-8080-exec-1] INFO  c.e.backend.service.ProductService - Product found with sku: LAP-001
```

---

#### **B. FILE Appender** 📁 (Rolling)

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/application.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
            <maxFileSize>10MB</maxFileSize>
        </timeBasedFileNamingAndTriggeringPolicy>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
</appender>
```

**What it does**: Writes logs to files with automatic rotation

**Rolling Strategy**:
1. **Time-based**: Creates new file every day at midnight
2. **Size-based**: If file exceeds 10MB, creates new file with index
3. **Retention**: Keeps last 30 days, deletes older files automatically

**File Naming Examples**:
```
logs/
├── application.log                 ← Current active log
├── application-2024-12-06.1.log   ← Today, first 10MB
├── application-2024-12-06.2.log   ← Today, second 10MB
├── application-2024-12-05.1.log   ← Yesterday
└── application-2024-11-07.1.log   ← Deleted after 30 days
```

**Why Rolling?**:
- Prevents single massive log file
- Easy to archive old logs
- Automatic cleanup saves disk space
- Can analyze specific time periods

---

#### **C. JSON Appender** 🔍 (Production)

```xml
<appender name="JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/application-json.log</file>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"application":"${application}"}</customFields>
    </encoder>
</appender>
```

**What it does**: Writes logs in **structured JSON format**

**Why JSON logs?**:
- Easy to parse programmatically
- Perfect for log aggregation (CloudWatch, Splunk, ELK)
- Searchable and filterable
- No regex parsing needed

**Example JSON Output**:
```json
{
  "@timestamp": "2024-12-06T10:30:45.123Z",
  "@version": "1",
  "message": "GET /api/v1/products/1",
  "logger_name": "com.example.backend.controller.ProductController",
  "thread_name": "http-nio-8080-exec-1",
  "level": "INFO",
  "level_value": 20000,
  "application": "backend-application",
  "stack_trace": null
}
```

**Use Case**: Send to **CloudWatch Logs Insights**:
```sql
fields @timestamp, level, message, logger_name
| filter level = "ERROR"
| sort @timestamp desc
| limit 100
```

---

### Log Levels: What Gets Logged

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

**Hierarchy** (each includes all above it):
```
TRACE (most verbose)
  ↓
DEBUG  ← Your app code (com.example.backend)
  ↓
INFO   ← Spring framework, Hibernate
  ↓
WARN   ← Root level minimum
  ↓
ERROR (least verbose)
```

**What this means**:
- **Your code** (`com.example.backend`): Shows DEBUG and above
- **Spring Web**: Shows INFO and above (no DEBUG)
- **Hibernate**: Shows INFO and above (no SQL queries)
- **Everything else**: Shows INFO and above

**Change for Development**:
```xml
<logger name="org.hibernate.SQL" level="DEBUG"/>  <!-- Show SQL queries -->
<logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE"/>  <!-- Show parameters -->
```

---

## 2. Application.yml Deep Dive

### File: `src/main/resources/application.yml`

This is the **brain** of your Spring Boot application. Let's understand every section:

---

### Section A: Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/backend_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: backend_user
    password: backend_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

**JDBC URL Parameters Explained**:
- `jdbc:mysql://` - Protocol (MySQL JDBC)
- `localhost:3306` - Host and port
- `/backend_db` - Database name
- `?useSSL=false` - Disable SSL (local dev only, **enable in production**)
- `allowPublicKeyRetrieval=true` - Allow retrieving public key for authentication
- `serverTimezone=UTC` - Set timezone to UTC (avoids timezone issues)

**Driver**: `com.mysql.cj.jdbc.Driver` (MySQL Connector/J 8.x)

---

### Section B: HikariCP Connection Pool

```yaml
hikari:
  maximum-pool-size: 10       # Max 10 DB connections
  minimum-idle: 5              # Keep 5 connections ready
  connection-timeout: 30000    # Wait 30s for connection before failing
  idle-timeout: 600000         # Close idle connections after 10 minutes
  max-lifetime: 1800000        # Recycle connections after 30 minutes
```

**How Connection Pooling Works**:

```
Request 1 ──→ Gets Connection #1 ──→ Use DB ──→ Return to Pool
Request 2 ──→ Reuses Connection #1 ──→ Use DB ──→ Return to Pool
Request 3 ──→ Gets Connection #2 ──→ Use DB ──→ Return to Pool
...
Request 11 ──→ WAITS (all 10 connections in use) ──→ Times out after 30s
```

**Why Connection Pooling?**:
- Opening new DB connection: **~100-200ms** 🐌
- Reusing pooled connection: **~1-2ms** 🚀
- **50-100x faster!**

**Tuning Guide**:
| Scenario | max-pool-size | min-idle |
|----------|---------------|----------|
| Low traffic (< 100 req/min) | 5-10 | 2-5 |
| Medium traffic (100-1000 req/min) | 10-20 | 5-10 |
| High traffic (> 1000 req/min) | 20-50 | 10-20 |

---

### Section C: JPA/Hibernate Configuration

```yaml
jpa:
  hibernate:
    ddl-auto: validate
```

**ddl-auto Options** (CRITICAL TO UNDERSTAND):

| Value | Behavior | Use Case |
|-------|----------|----------|
| `validate` | ✅ Only checks schema matches entities | **Production** |
| `update` | ⚠️ Modifies schema automatically | Dev (risky!) |
| `create` | ❌ Drops all tables and recreates | Never use |
| `create-drop` | ❌ Drops tables on shutdown | Tests only |
| `none` | No action | Manual migrations |

**Best Practice**: Use `validate` + Flyway migrations

```yaml
  show-sql: false  # Set to true to see SQL in logs
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQLDialect  # MySQL-specific SQL
      format_sql: true        # Pretty-print SQL queries
      jdbc:
        batch_size: 20        # Batch up to 20 inserts/updates together
      order_inserts: true     # Group inserts for better batching
      order_updates: true     # Group updates for better batching
  open-in-view: false         # Prevents lazy loading issues
```

**Batch Operations Example**:

Without batching:
```sql
INSERT INTO products VALUES (...);  -- Query 1
INSERT INTO products VALUES (...);  -- Query 2
INSERT INTO products VALUES (...);  -- Query 3
-- 100 separate queries = 100 round trips!
```

With batching (`batch_size: 20`):
```sql
INSERT INTO products VALUES (...), (...), (...), ...; -- 20 rows in 1 query
INSERT INTO products VALUES (...), (...), (...), ...; -- Next 20 rows
-- 5 queries instead of 100!
```

**Performance**: ~5-10x faster for bulk operations!

---

### Section D: Flyway Migrations

```yaml
flyway:
  enabled: true
  baseline-on-migrate: true
  locations: classpath:db/migration
  schemas: backend_db
```

**How Flyway Works**:

1. **First Run**: Creates `flyway_schema_history` table
```sql
CREATE TABLE flyway_schema_history (
    version VARCHAR(50),
    description VARCHAR(200),
    script VARCHAR(1000),
    checksum INT,
    installed_on TIMESTAMP,
    success BOOLEAN
);
```

2. **Scans for Migrations**: Looks in `src/main/resources/db/migration/`
```
V1__create_products_table.sql     ← Run this
V2__add_categories_table.sql       ← Then this
V3__add_product_images.sql         ← Then this
```

3. **Tracks Execution**: Records in `flyway_schema_history`
```sql
| version | description          | success |
|---------|---------------------|---------|
| 1       | create products table| true    |
| 2       | add categories table | true    |
```

4. **Never Reruns**: Checks version number before running

**Benefits**:
- ✅ Version control for database
- ✅ Consistent schema across environments
- ✅ Rollback support (create down migrations)
- ✅ Safe to run multiple times

---

### Section E: Redis Configuration

```yaml
data:
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8   # Max 8 Redis connections
        max-idle: 8     # Keep 8 idle connections
        min-idle: 2     # Minimum 2 connections always ready
```

**Lettuce vs Jedis**:
- **Lettuce** ✅ (Default, recommended)
  - Thread-safe
  - Supports async operations
  - Better performance
  - Reactive support

- **Jedis** (Legacy)
  - Not thread-safe
  - Blocking operations
  - Simpler API

---

## 3. Redis Cache Configuration

### How Your Application Connects to Redis

```
Application Startup
       ↓
CacheConfig.java
       ↓
Creates RedisConnectionFactory
       ↓
Reads: spring.data.redis.host (localhost)
Reads: spring.data.redis.port (6379)
       ↓
Establishes Connection Pool (Lettuce)
       ↓
Creates RedisCacheManager
       ↓
Configures Cache TTLs:
  - products: 30 minutes
  - productById: 1 hour
       ↓
Ready to Cache! ✅
```

### Cache Flow Diagram

```
┌─────────────────────────────────────────────┐
│ @Cacheable(value="productById", key="#id")  │
└───────────────────┬─────────────────────────┘
                    │
                    ↓
         ┌──────────────────────┐
         │ Check Redis for key  │
         │ "productById::1"     │
         └──────┬───────┬───────┘
                │       │
        Found   │       │   Not Found
         (HIT)  │       │   (MISS)
                ↓       ↓
         ┌──────┐   ┌────────────┐
         │Return│   │Query       │
         │from  │   │Database    │
         │Redis │   └─────┬──────┘
         └──────┘         │
                          ↓
                   ┌──────────────┐
                   │Store in Redis│
                   │with TTL      │
                   └──────┬───────┘
                          │
                          ↓
                    Return Result
```

---

## 4. AWS Production Setup

### Complete AWS ElastiCache Setup Guide

#### **Step 1: Create ElastiCache Cluster**

**Using AWS Console**:
1. Navigate to **ElastiCache** service
2. Click **Create cluster** → **Redis**
3. **Configuration**:
   - **Name**: `backend-redis-prod`
   - **Engine**: Redis 7.0
   - **Node type**: `cache.t3.medium` (2 vCPU, 3.09 GB)
   - **Number of nodes**: 1 (or 2-3 for replication)
4. **Advanced Settings**:
   - ✅ Enable **Multi-AZ** (automatic failover)
   - ✅ Enable **Encryption in-transit** (TLS)
   - ✅ Enable **Encryption at-rest**
   - ✅ Enable **Auth token** (password)
   - ✅ Set **Auto-failover** to enabled
5. **Subnet Group**: Select private subnets
6. **Security Group**: Create/select security group
7. **Backup**: Enable daily backups
8. Create cluster

---

#### **Step 2: Configure Security Group**

**ElastiCache Security Group**:
```
Inbound Rules:
┌──────────────┬──────┬─────────────────────────────┐
│ Type         │ Port │ Source                      │
├──────────────┼──────┼─────────────────────────────┤
│ Custom TCP   │ 6379 │ sg-app123 (App sec group)  │
└──────────────┴──────┴─────────────────────────────┘

Outbound Rules: Allow all
```

---

#### **Step 3: Get Connection Endpoint**

After creation, get the endpoint:

**AWS Console**:
```
Primary Endpoint: my-cluster.cache.amazonaws.com:6379
```

**AWS CLI**:
```bash
aws elasticache describe-cache-clusters \
    --cache-cluster-id backend-redis-prod \
    --show-cache-node-info \
    --query 'CacheClusters[0].CacheNodes[0].Endpoint'
```

---

#### **Step 4: Store Credentials in AWS Secrets Manager**

```bash
# Create secret
aws secretsmanager create-secret \
    --name prod/backend/redis \
    --description "Redis connection details for backend application" \
    --secret-string '{
      "host": "my-cluster.cache.amazonaws.com",
      "port": 6379,
      "password": "your-strong-auth-token-here",
      "ssl": true
    }'
```

---

#### **Step 5: Application Configuration**

**Option A: Environment Variables (Recommended for Containers)**

For **ECS Task Definition**:
```json
{
  "environment": [
    {
      "name": "SPRING_PROFILES_ACTIVE",
      "value": "prod"
    },
    {
      "name": "REDIS_HOST",
      "value": "my-cluster.cache.amazonaws.com"
    },
    {
      "name": "REDIS_PORT",
      "value": "6379"
    },
    {
      "name": "REDIS_SSL_ENABLED",
      "value": "true"
    }
  ],
  "secrets": [
    {
      "name": "REDIS_PASSWORD",
      "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:prod/backend/redis"
    }
  ]
}
```

For **EC2 User Data**:
```bash
#!/bin/bash
export SPRING_PROFILES_ACTIVE=prod
export REDIS_HOST=$(aws secretsmanager get-secret-value --secret-id prod/backend/redis --query 'SecretString' --output text | jq -r '.host')
export REDIS_PASSWORD=$(aws secretsmanager get-secret-value --secret-id prod/backend/redis --query 'SecretString' --output text | jq -r '.password')

java -jar /opt/backend/application.jar
```

**Option B: application-prod.yml**

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:default-host.cache.amazonaws.com}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD}
      ssl:
        enabled: ${REDIS_SSL_ENABLED:true}
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 4
          max-wait: 5000ms
```

---

#### **Step 6: IAM Permissions (If Using Secrets Manager)**

Your EC2/ECS task needs IAM permissions:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": [
        "arn:aws:secretsmanager:us-east-1:123456789:secret:prod/backend/redis-*"
      ]
    }
  ]
}
```

---

## 5. Configuration Profiles

Spring Boot supports multiple configuration files for different environments.

### Profile Loading Order

```
application.yml (base config, always loaded)
       ↓
application-{profile}.yml (profile-specific, overrides base)
       ↓
Environment Variables (overrides all)
```

---

### Example: Running with Different Profiles

#### **Local Development**:
```bash
# Uses: application.yml + application-local.yml
./gradlew bootRun --args='--spring.profiles.active=local'

# Redis: localhost:6379
# MySQL: localhost:3306
# Logs: DEBUG level
# Cache TTL: 10 minutes
```

#### **Production**:
```bash
# Uses: application.yml + application-prod.yml + environment variables
java -jar application.jar --spring.profiles.active=prod

# Redis: ElastiCache endpoint (from env var)
# MySQL: RDS endpoint (from env var)
# Logs: INFO level
# Cache TTL: 1 hour
# SSL: Enabled
```

#### **Testing**:
```bash
# Uses: application.yml + application-test.yml
./gradlew test

# Redis: DISABLED (in-memory cache)
# MySQL: H2 in-memory database
# Logs: WARN level
```

---

### Environment-Specific Configurations

#### **application-local.yml** (Local Dev)
```yaml
spring:
  data:
    redis:
      host: localhost  # Docker Compose
logging:
  level:
    com.example.backend: DEBUG       # Verbose
    org.hibernate.SQL: DEBUG          # Show SQL queries
```

#### **application-prod.yml** (Production)
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}  # From environment
      ssl:
        enabled: true      # Encrypted
      password: ${REDIS_PASSWORD}
logging:
  level:
    com.example.backend: INFO        # Less verbose
    org.hibernate.SQL: WARN           # No SQL queries
server:
  error:
    include-stacktrace: never         # Never expose stack traces
```

#### **application-test.yml** (Testing)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # In-memory database
  flyway:
    enabled: false            # No migrations for tests
```

---

## 🎯 Quick Reference

### How to Check Active Configuration

```bash
# View active profile
curl http://localhost:8080/actuator/env | jq '.activeProfiles'

# View all Redis configuration
curl http://localhost:8080/actuator/configprops | jq '.contexts.application.beans.redisProperties'

# View cache configuration
curl http://localhost:8080/actuator/caches
```

### How to Test Redis Connection

```bash
# Local
docker exec -it backend-redis redis-cli ping

# Production (if accessible)
redis-cli -h my-cluster.cache.amazonaws.com -p 6379 -a your-auth-token --tls ping
```

### How to Monitor Cache Performance

```bash
# Cache hit rate
curl http://localhost:8080/actuator/metrics/cache.gets | jq

# View cached keys in Redis
docker exec -it backend-redis redis-cli KEYS "*"

# Monitor live Redis operations
docker exec -it backend-redis redis-cli MONITOR
```

---

## 📊 Complete Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│                     Client Request                       │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│                  ProductController                       │
│  - Receives HTTP request                                │
│  - Validates input                                      │
│  - Logs request                                         │
└────────────────────────┬────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│                   ProductService                         │
│  - @Cacheable annotation checks Redis first            │
└────────────────────────┬────────────────────────────────┘
                         │
          ┌──────────────┴──────────────┐
          │                             │
   Cache HIT                      Cache MISS
          │                             │
          ↓                             ↓
┌──────────────────┐         ┌──────────────────┐
│  Redis Cache     │         │  ProductRepository│
│  - Return cached │         │  - Query Database │
│    data in 2ms   │         └─────────┬─────────┘
└──────────────────┘                   │
                                       ↓
                             ┌──────────────────┐
                             │  MySQL Database  │
                             │  - Execute query │
                             │  - Return results│
                             └─────────┬─────────┘
                                       │
                                       ↓
                             ┌──────────────────┐
                             │  Store in Redis  │
                             │  with TTL        │
                             └──────────────────┘
```

---

## 🎓 Learning Summary

### Logback (Logging)
- **3 Appenders**: Console (terminal), File (disk), JSON (aggregation)
- **Rolling**: Creates new files daily or when > 10MB
- **Levels**: DEBUG for dev, INFO for prod
- **JSON Format**: Perfect for CloudWatch Logs Insights

### Application.yml
- **HikariCP**: Connection pooling (reuses DB connections)
- **JPA**: Use `validate` + Flyway for schema management
- **Profiles**: Different configs for local/prod/test
- **Environment Variables**: Override any config value

### Redis Cache
- **Currently**: Docker Compose Redis (localhost:6379)
- **Production**: AWS ElastiCache with SSL + AUTH
- **Benefits**: 10-20x faster responses, reduced DB load
- **TTL**: products (30min), productById (1 hour)

### Profiles
- **local**: Docker Compose, verbose logging
- **prod**: ElastiCache, minimal logging, SSL
- **test**: In-memory cache, H2 database

---

## 📝 Next Steps

1. **Read**: `REDIS_AND_CACHE_GUIDE.md` for detailed Redis setup
2. **Read**: `UNDERSTANDING_CONFIGURATIONS.md` for detailed config explanations
3. **Test**: Start Docker Compose and test caching locally
4. **Deploy**: Follow AWS ElastiCache setup for production

Your application is now **fully documented** and **production-ready**! 🚀
