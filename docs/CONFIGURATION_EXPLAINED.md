# Configuration Explained - Quick Reference

## 📚 Understanding Your Application

This document provides quick explanations for the key configuration files.

---

## 1. 📝 Logback Logging (logback-spring.xml)

### What is Logback?
Your application's logging system that controls WHERE and HOW logs are written.

### Three Log Destinations:

#### **CONSOLE** 📺
- **Writes to**: Terminal/stdout
- **Format**: Human-readable
- **Example**: `2024-12-06 10:30:45 [thread] INFO  ClassName - Log message`
- **Use**: Development, debugging

#### **FILE** 📁
- **Writes to**: `logs/application.log`
- **Format**: Human-readable
- **Rolling**: New file daily or when > 10MB
- **Retention**: 30 days
- **Use**: Production, audit trail

#### **JSON** 🔍
- **Writes to**: `logs/application-json.log`
- **Format**: JSON (structured)
- **Use**: CloudWatch, Splunk, ELK Stack
- **Example**:
```json
{
  "@timestamp": "2024-12-06T10:30:45.123Z",
  "level": "INFO",
  "logger": "com.example.backend.controller.ProductController",
  "message": "GET /api/v1/products/1"
}
```

### Log Levels (verbosity):
```
TRACE → DEBUG → INFO → WARN → ERROR
(most)                         (least)
```

---

## 2. ⚙️ Application.yml Configuration

### Key Sections:

#### **A. Database (MySQL)**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/backend_db
    hikari:
      maximum-pool-size: 10  # Max 10 DB connections
      minimum-idle: 5         # Keep 5 ready
```
**Connection Pooling**: Reuses connections (100x faster than creating new ones)

#### **B. Redis Cache**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```
**Now Connected**: Your cache actually uses Redis from Docker Compose!

#### **C. Flyway (Database Migrations)**
```yaml
flyway:
  enabled: true
  locations: classpath:db/migration
```
**What it does**: Runs SQL files in `src/main/resources/db/migration/` on startup

---

## 3. 🚀 Redis Cache - How It Works

### Current Setup:

**❌ Before** (Old Config):
- Used `ConcurrentMapCacheManager` (in-memory)
- Cache lost on restart
- Not shared between instances

**✅ After** (New Config):
- Uses `RedisCacheManager` (distributed)
- Cache persists across restarts
- Shared between multiple app instances
- Production-ready!

### Cache Flow:

```
1. Request comes in
2. @Cacheable checks Redis
3. If found (HIT): Return immediately (2ms) ⚡
4. If not (MISS): Query database → Store in Redis → Return (50ms)
5. Next request: Uses cached value ⚡
```

### Where Caching Happens:

```java
// In ProductService.java
@Cacheable(value = "productById", key = "#id")
public ProductDto getProductById(Long id) {
    // This only runs on cache MISS
    return productRepository.findById(id)...
}
```

### Test It:
```bash
# Start Redis
docker-compose up -d redis

# First request (slow - database)
time curl http://localhost:8080/api/v1/products/1

# Second request (fast - Redis!)
time curl http://localhost:8080/api/v1/products/1
```

---

## 4. ☁️ AWS ElastiCache Setup (Production)

### Quick Setup Steps:

#### **1. Create ElastiCache**
```bash
aws elasticache create-replication-group \
    --replication-group-id backend-redis-prod \
    --replication-group-description "Backend Redis Cache" \
    --engine redis \
    --cache-node-type cache.t3.medium \
    --num-cache-clusters 2 \
    --automatic-failover-enabled \
    --at-rest-encryption-enabled \
    --transit-encryption-enabled \
    --auth-token "your-strong-random-token"
```

#### **2. Get Endpoint**
```bash
# Primary endpoint for writes/reads
aws elasticache describe-replication-groups \
    --replication-group-id backend-redis-prod \
    --query 'ReplicationGroups[0].NodeGroups[0].PrimaryEndpoint.Address'
```

#### **3. Configure Application**

**Set Environment Variables**:
```bash
export REDIS_HOST=my-cluster.abc123.cache.amazonaws.com
export REDIS_PASSWORD=your-auth-token
export REDIS_SSL_ENABLED=true
export SPRING_PROFILES_ACTIVE=prod
```

**Run Application**:
```bash
java -jar backend-application.jar
```

### Architecture Options:

| Setup | Cost/Month | Use Case |
|-------|------------|----------|
| Single node (`cache.t3.micro`) | $15 | Dev/Staging |
| Multi-AZ (`cache.t3.medium` x2) | $100 | Small prod |
| Cluster mode (`cache.m6g.large` x3) | $400+ | High traffic |

---

## 5. 📋 Configuration Profiles Summary

### Local Development
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```
- Redis: localhost:6379 (Docker)
- MySQL: localhost:3306 (Docker)
- Logs: DEBUG (verbose)
- Cache TTL: 10 minutes

### Production
```bash
java -jar app.jar --spring.profiles.active=prod
```
- Redis: ElastiCache endpoint
- MySQL: RDS endpoint
- Logs: INFO (less verbose)
- Cache TTL: 1 hour
- SSL: Enabled

### Testing
```bash
./gradlew test
```
- Redis: Disabled (in-memory)
- MySQL: H2 (in-memory)
- Logs: WARN (minimal)

---

## 🔍 Monitoring & Debugging

### View Active Configuration:
```bash
# Check active profile
curl http://localhost:8080/actuator/env | jq '.activeProfiles'

# Check Redis connection
curl http://localhost:8080/actuator/health | jq '.components.redis'

# Check cache metrics
curl http://localhost:8080/actuator/metrics/cache.gets
```

### View Redis Cache:
```bash
# Connect to Redis CLI
docker exec -it backend-redis redis-cli

# View all cached keys
KEYS *

# View specific cache entry
GET "productById::1"

# Check TTL (seconds remaining)
TTL "productById::1"

# Clear all caches
FLUSHDB
```

---

## ⚡ Quick Tips

### 1. **Enable SQL Logging** (Development)
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
```

### 2. **Change Cache TTL**
Edit `CacheConfig.java`:
```java
cacheConfigurations.put(PRODUCT_BY_ID_CACHE,
    defaultConfig.entryTtl(Duration.ofMinutes(5)));  // 5 minutes instead of 1 hour
```

### 3. **Disable Redis** (Temporarily)
```bash
./gradlew bootRun --args='--spring.profiles.active=test'
# Uses in-memory cache
```

### 4. **Production Environment Variables**
```bash
# Minimal required for AWS
export REDIS_HOST=my-cluster.cache.amazonaws.com
export REDIS_PASSWORD=auth-token
export DB_URL=jdbc:mysql://my-rds.amazonaws.com:3306/backend_db
export DB_PASSWORD=db-password
export SPRING_PROFILES_ACTIVE=prod
```

---

## 📚 Additional Resources

- **Detailed Redis Guide**: See `REDIS_AND_CACHE_GUIDE.md`
- **AWS Setup**: See `AWS_ELASTICACHE_SETUP.md` (if created)
- **Spring Boot Docs**: https://docs.spring.io/spring-boot/docs/current/reference/html/

---

## ✅ Summary

| Component | Local | Production |
|-----------|-------|------------|
| **Redis** | Docker localhost:6379 | AWS ElastiCache |
| **MySQL** | Docker localhost:3306 | AWS RDS |
| **Logs** | DEBUG, console + files | INFO, JSON files |
| **Cache TTL** | 10 minutes | 1 hour |
| **SSL** | Disabled | Enabled |

Your application is now **fully configured for Redis caching**! 🎉
