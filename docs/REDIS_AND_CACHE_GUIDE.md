# Redis & Cache Configuration Guide

## 📋 Overview

Your application now supports **Redis-based distributed caching** with configurations for:
- ✅ Local development (Docker Compose)
- ✅ Production (AWS ElastiCache)
- ✅ Test environment (in-memory)

---

## 🔄 How Caching Works in Your Application

### 1. **Cache Annotations in Service Layer**

```java
@Service
@Transactional(readOnly = true)
public class ProductService {

    // This method caches results in Redis
    @Cacheable(value = PRODUCT_BY_ID_CACHE, key = "#id")
    public ProductDto getProductById(Long id) {
        // This code only runs if cache MISS
        // On cache HIT, returns cached value immediately
        Product product = productRepository.findById(id)...
        return productMapper.toDto(product);
    }

    // This method evicts (clears) cache entries
    @CacheEvict(value = {PRODUCTS_CACHE, PRODUCT_BY_ID_CACHE}, allEntries = true)
    public ProductDto createProduct(ProductDto productDto) {
        // After creating a product, clear all caches
        // Next request will fetch fresh data
        ...
    }
}
```

### 2. **How @Cacheable Works**

**First Request** (Cache MISS):
```
Client → Controller → Service → @Cacheable → Repository → Database
                                     ↓
                                  Redis STORE
                                     ↓
Client ← Response ←──────────────────┘
```

**Subsequent Requests** (Cache HIT):
```
Client → Controller → Service → @Cacheable → Redis GET → Client
                                     ↑
                            (Skips Database!)
```

### 3. **Cache Keys in Redis**

When you call `getProductById(1)`:

**Redis Key Generated**: `productById::1`
**Redis Value**: JSON serialized ProductDto

```bash
# View in Redis CLI
redis-cli
> KEYS *
1) "productById::1"
2) "productById::2"
3) "products::PageRequest..."

> GET "productById::1"
"{\"@class\":\"com.example.backend.dto.ProductDto\",\"id\":1,\"name\":\"Laptop\"...}"

> TTL "productById::1"
3542  # Seconds remaining before expiration
```

### 4. **Cache TTL (Time To Live)**

Different caches have different lifespans:

| Cache Name | TTL | Reason |
|------------|-----|--------|
| `products` | 30 min | List changes frequently (new products, updates) |
| `productById` | 1 hour | Individual products are more stable |

**Configured in CacheConfig.java**:
```java
// Products cache: 30 minutes
cacheConfigurations.put(PRODUCTS_CACHE,
        defaultConfig.entryTtl(Duration.ofMinutes(30)));

// Product by ID: 1 hour
cacheConfigurations.put(PRODUCT_BY_ID_CACHE,
        defaultConfig.entryTtl(Duration.ofHours(1)));
```

---

## 🐳 Local Development Setup (Docker Compose)

### **Current Setup**

Your `docker-compose.yml` already includes Redis:
```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  healthcheck:
    test: ["CMD", "redis-cli", "ping"]
```

### **How to Use**

1. **Start Redis**:
```bash
docker-compose up -d redis
```

2. **Verify Redis is Running**:
```bash
docker exec -it backend-redis redis-cli ping
# Expected output: PONG
```

3. **Run Application**:
```bash
./gradlew bootRun
# or with profile:
./gradlew bootRun --args='--spring.profiles.active=local'
```

4. **Test Cache**:
```bash
# First request - goes to database
curl http://localhost:8080/api/v1/products/1

# Second request - returns from Redis (much faster!)
curl http://localhost:8080/api/v1/products/1
```

5. **Monitor Redis**:
```bash
# Connect to Redis CLI
docker exec -it backend-redis redis-cli

# View all keys
KEYS *

# View cache statistics
INFO stats

# Clear all caches (for testing)
FLUSHDB
```

---

## ☁️ Production Setup (AWS ElastiCache)

### **Option 1: ElastiCache for Redis (Recommended)**

#### **Step 1: Create ElastiCache Cluster**

Using AWS CLI:
```bash
aws elasticache create-cache-cluster \
    --cache-cluster-id backend-redis-prod \
    --engine redis \
    --cache-node-type cache.t3.micro \
    --num-cache-nodes 1 \
    --engine-version 7.0 \
    --port 6379 \
    --security-group-ids sg-xxxxx \
    --preferred-maintenance-window sun:05:00-sun:06:00
```

Or using AWS Console:
1. Go to **ElastiCache** → **Redis clusters**
2. Click **Create**
3. Choose **Redis** engine
4. Select node type (e.g., `cache.t3.medium`)
5. Configure security groups
6. Enable **Encryption in-transit** (SSL/TLS)
7. Enable **Auth token** for authentication

#### **Step 2: Get Connection Details**

After creation, note:
- **Primary Endpoint**: `your-cluster.cache.amazonaws.com`
- **Port**: `6379`
- **Auth Token**: If enabled

#### **Step 3: Configure Application**

**Method A: Using Environment Variables** (Recommended)
```bash
export REDIS_HOST=your-elasticache-cluster.cache.amazonaws.com
export REDIS_PORT=6379
export REDIS_PASSWORD=your-auth-token
export REDIS_SSL_ENABLED=true
export SPRING_PROFILES_ACTIVE=prod

java -jar backend-application.jar
```

**Method B: Using application-prod.yml**

Update `src/main/resources/application-prod.yml`:
```yaml
spring:
  data:
    redis:
      host: your-elasticache-cluster.cache.amazonaws.com
      port: 6379
      ssl:
        enabled: true
      password: your-auth-token  # If AUTH enabled
```

Run with:
```bash
java -jar backend-application.jar --spring.profiles.active=prod
```

#### **Step 4: Security Group Configuration**

Ensure your ElastiCache security group allows:
- **Inbound**: Port 6379 from your application's security group
- **Encryption**: Enable in-transit encryption for production

Example security group rule:
```
Type: Custom TCP
Port: 6379
Source: sg-xxxxx (your application security group)
```

---

## 🔧 Configuration Profiles Explained

### **Profile: default (application.yml)**

```yaml
spring:
  data:
    redis:
      host: localhost  # Local Docker Compose
      port: 6379
```

**Use When**: Local development, default behavior

**Run**: `./gradlew bootRun`

---

### **Profile: local (application-local.yml)**

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    redis:
      time-to-live: 600000  # 10 minutes (shorter for dev)
```

**Use When**: Explicit local development with detailed logging

**Run**: `./gradlew bootRun --args='--spring.profiles.active=local'`

---

### **Profile: prod (application-prod.yml)**

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}  # Environment variable
      port: ${REDIS_PORT:6379}
      ssl:
        enabled: true      # Encrypted connection
      password: ${REDIS_PASSWORD}
```

**Use When**: Production deployment (AWS, etc.)

**Run**: `java -jar app.jar --spring.profiles.active=prod`

---

### **Profile: test (application-test.yml)**

```yaml
# Redis is DISABLED for tests
# Uses in-memory cache instead (ConcurrentHashMap)
```

**Use When**: Running unit/integration tests

**Run**: `./gradlew test` (automatically uses test profile)

---

## 🏗️ AWS ElastiCache Architecture Examples

### **Architecture 1: Single Node (Development/Staging)**

```
┌─────────────────┐
│  Spring Boot    │
│  Application    │
│  (EC2/ECS)      │
└────────┬────────┘
         │
         │ Port 6379
         ↓
┌─────────────────┐
│  ElastiCache    │
│  Redis          │
│  (Single Node)  │
└─────────────────┘
```

**Configuration**:
- Node type: `cache.t3.micro` or `cache.t3.small`
- Cost: ~$15-30/month
- Good for: Dev, staging, low-traffic prod

---

### **Architecture 2: Cluster Mode (Production)**

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Spring Boot │  │ Spring Boot │  │ Spring Boot │
│ Instance 1  │  │ Instance 2  │  │ Instance 3  │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       │                │                │
       └────────────────┼────────────────┘
                        │
                        ↓
              ┌──────────────────┐
              │  ElastiCache     │
              │  Redis Cluster   │
              │  (3 Shards)      │
              │  Each with       │
              │  Replica         │
              └──────────────────┘
```

**Configuration**:
- Cluster mode enabled
- Multiple shards with replicas
- Automatic failover
- Higher throughput
- Node type: `cache.m6g.large` or better
- Cost: ~$200-500/month
- Good for: High-traffic production

---

### **Architecture 3: Multi-AZ with Replication**

```
        ┌─────────────────┐
        │  Application    │
        │  Load Balancer  │
        └────────┬────────┘
                 │
     ┌───────────┴───────────┐
     │                       │
     ↓                       ↓
┌─────────┐           ┌─────────┐
│  App    │           │  App    │
│  AZ-1   │           │  AZ-2   │
└────┬────┘           └────┬────┘
     │                     │
     └──────────┬──────────┘
                │
        ┌───────┴────────┐
        │                │
        ↓                ↓
    ┌────────┐      ┌────────┐
    │Primary │─────→│Replica │
    │Redis   │      │Redis   │
    │ AZ-1   │      │ AZ-2   │
    └────────┘      └────────┘
    Automatic Failover
```

**Configuration**:
- Multi-AZ replication group
- Automatic failover
- Read replicas for scaling reads
- Good for: Mission-critical production

---

## 🛠️ Complete Configuration Examples

### **Example 1: AWS ElastiCache (Standard)**

**application-prod.yml**:
```yaml
spring:
  data:
    redis:
      host: my-prod-redis.cache.amazonaws.com
      port: 6379
      ssl:
        enabled: true
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 4
```

**Environment Variables**:
```bash
export REDIS_HOST=my-prod-redis.cache.amazonaws.com
export REDIS_PORT=6379
export REDIS_SSL_ENABLED=true
```

---

### **Example 2: ElastiCache with AUTH Token**

**application-prod.yml**:
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST}
      port: 6379
      password: ${REDIS_AUTH_TOKEN}  # From AWS Secrets Manager
      ssl:
        enabled: true
```

**Retrieve from AWS Secrets Manager**:
```bash
# Get auth token
aws secretsmanager get-secret-value \
    --secret-id prod/redis/auth-token \
    --query SecretString \
    --output text

# Set as environment variable
export REDIS_AUTH_TOKEN=$(aws secretsmanager get-secret-value ...)
```

---

### **Example 3: ElastiCache Cluster Mode**

**application-prod.yml**:
```yaml
spring:
  data:
    redis:
      cluster:
        nodes:
          - my-cluster.cache.amazonaws.com:6379
        max-redirects: 3
      ssl:
        enabled: true
      password: ${REDIS_AUTH_TOKEN}
```

**CacheConfig.java** (Update for cluster):
```java
@Bean
@Profile("prod-cluster")
public RedisConnectionFactory redisClusterConnectionFactory() {
    RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration()
            .clusterNode(redisHost, redisPort);

    if (redisPassword != null) {
        clusterConfig.setPassword(redisPassword);
    }

    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .useSsl()
            .build();

    return new LettuceConnectionFactory(clusterConfig, clientConfig);
}
```

---

## 📊 Monitoring Redis Cache

### **1. Application Logs**

The application logs cache operations:
```
DEBUG c.e.backend.service.ProductService - Fetching product with id: 1
# If cache hit, you'll see very fast response time
# If cache miss, you'll see database query
```

### **2. Redis Metrics via Actuator**

Access: `http://localhost:8080/actuator/metrics`

Available metrics:
```
cache.gets
cache.puts
cache.evictions
cache.size
```

**Example**:
```bash
curl http://localhost:8080/actuator/metrics/cache.gets
# Shows cache hit/miss statistics
```

### **3. Redis CLI Monitoring**

```bash
# Connect to Redis
docker exec -it backend-redis redis-cli

# Monitor real-time operations
MONITOR

# View cache keys
KEYS backend:cache:*

# Get cache statistics
INFO stats

# View memory usage
INFO memory
```

### **4. AWS CloudWatch (Production)**

ElastiCache automatically sends metrics to CloudWatch:
- **CacheHits / CacheMisses** - Cache effectiveness
- **CPUUtilization** - CPU usage
- **NetworkBytesIn/Out** - Network traffic
- **CurrConnections** - Active connections
- **Evictions** - Items removed due to memory limits

---

## 🔐 Security Best Practices

### **1. Enable AUTH Token**

**AWS ElastiCache**:
```bash
aws elasticache create-cache-cluster \
    --auth-token "your-strong-random-token" \
    --transit-encryption-enabled
```

**Application Configuration**:
```yaml
spring:
  data:
    redis:
      password: ${REDIS_AUTH_TOKEN}
```

### **2. Enable Encryption**

**In-Transit Encryption**:
```yaml
spring:
  data:
    redis:
      ssl:
        enabled: true
```

**At-Rest Encryption** (ElastiCache setting):
- Enable during cluster creation
- Uses AWS KMS for encryption keys

### **3. Network Security**

- ✅ Place ElastiCache in **private subnet**
- ✅ Use **security groups** to restrict access
- ✅ Only allow connections from application security group
- ✅ Enable **VPC** for network isolation

### **4. Use AWS Secrets Manager**

**Store credentials securely**:
```bash
aws secretsmanager create-secret \
    --name prod/redis/config \
    --secret-string '{
      "host": "my-cluster.cache.amazonaws.com",
      "port": 6379,
      "password": "strong-random-token"
    }'
```

**Retrieve in application**:
```java
// Use AWS SDK to fetch secrets
// Or use Spring Cloud AWS Secrets Manager
```

---

## 🎯 Testing Cache Functionality

### **Test 1: Verify Cache is Working**

```bash
# 1. Start application with Redis
docker-compose up -d
./gradlew bootRun

# 2. Make first request (slow - database hit)
time curl http://localhost:8080/api/v1/products/1

# 3. Make second request (fast - Redis hit)
time curl http://localhost:8080/api/v1/products/1

# You should see significant speed improvement!
```

### **Test 2: Verify Cache Eviction**

```bash
# 1. Get product (caches it)
curl http://localhost:8080/api/v1/products/1

# 2. Update product (evicts cache)
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Name", ...}'

# 3. Get product again (cache miss, fetches from DB)
curl http://localhost:8080/api/v1/products/1
```

### **Test 3: Verify Redis Connection**

```bash
# Check Redis keys
docker exec -it backend-redis redis-cli KEYS "*"

# Should see keys like:
# "productById::1"
# "productById::2"
```

---

## 🚀 Switching Between Cache Implementations

### **Use Redis (Default)**
```bash
./gradlew bootRun
# Uses Redis at localhost:6379
```

### **Use In-Memory Cache (Tests)**
```bash
./gradlew test
# Automatically uses in-memory cache via @Profile("test")
```

### **Production with ElastiCache**
```bash
export REDIS_HOST=my-elasticache.cache.amazonaws.com
export REDIS_PASSWORD=my-auth-token
export REDIS_SSL_ENABLED=true
export SPRING_PROFILES_ACTIVE=prod

java -jar backend-application.jar
```

---

## 📈 Performance Comparison

### **Without Cache**:
- Database query: ~50-100ms
- Network overhead: ~10-20ms
- **Total**: ~60-120ms per request

### **With Redis Cache (Hit)**:
- Redis lookup: ~1-5ms
- Network overhead: ~1-2ms
- **Total**: ~2-7ms per request

### **Speed Improvement**: **10-20x faster!** 🚀

---

## 🔍 Troubleshooting

### **Issue: Can't Connect to Redis**

```bash
# Check if Redis is running
docker ps | grep redis

# Check Redis logs
docker logs backend-redis

# Test connection
docker exec -it backend-redis redis-cli ping

# Check application logs
tail -f logs/application.log | grep -i redis
```

### **Issue: Cache Not Working**

1. **Check profile is not 'test'**:
```bash
# Verify active profile
curl http://localhost:8080/actuator/env | grep "activeProfiles"
```

2. **Check Redis connection**:
```bash
# View Spring Boot Redis metrics
curl http://localhost:8080/actuator/metrics/cache.gets
```

3. **Check cache annotations**:
```java
// Ensure @EnableCaching is present on main class or config
// Ensure methods have @Cacheable annotation
```

### **Issue: AWS ElastiCache Connection Timeout**

1. **Check security group rules**
2. **Verify application is in same VPC**
3. **Check subnet routing**
4. **Enable VPC peering if needed**

---

## 💡 Best Practices

### **1. Cache Key Design**
```java
// Good: Specific, predictable keys
@Cacheable(value = "productById", key = "#id")

// Bad: Complex objects as keys
@Cacheable(value = "products", key = "#pageable")  // May not serialize well
```

### **2. TTL Configuration**
- ⏱️ **Short TTL** (5-10 min): Frequently changing data
- ⏱️ **Medium TTL** (30-60 min): Semi-static data
- ⏱️ **Long TTL** (2-24 hours): Rarely changing data

### **3. Cache Eviction Strategy**
```java
// Evict specific entry
@CacheEvict(value = "productById", key = "#id")

// Evict all entries in cache
@CacheEvict(value = "products", allEntries = true)

// Evict after method execution
@CacheEvict(afterInvocation = true)
```

### **4. Memory Management**

Set max memory in Redis:
```bash
# In docker-compose.yml
command: redis-server --maxmemory 256mb --maxmemory-policy allkeys-lru
```

**Eviction Policies**:
- `allkeys-lru` - Remove least recently used keys (recommended)
- `volatile-ttl` - Remove keys with shortest TTL
- `noeviction` - Return errors when memory full

---

## 📝 Summary

✅ **Local Dev**: Redis via Docker Compose
✅ **Production**: AWS ElastiCache with SSL/TLS
✅ **Testing**: In-memory cache (isolated tests)
✅ **Profiles**: Easy switching between environments
✅ **Monitoring**: Actuator metrics + CloudWatch
✅ **Security**: AUTH token + encryption

Your application is now configured for **production-grade distributed caching**! 🎉
