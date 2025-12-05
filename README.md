# Backend Application - Spring Boot Template

A production-ready Spring Boot backend application template demonstrating best practices for building scalable monolithic applications.

## 🚀 Features

- **Spring Boot 3.2.0** with Java 17
- **Gradle Kotlin DSL** for build configuration
- **MySQL 8.0** database with HikariCP connection pooling
- **Flyway** for database migrations
- **Lombok** for reducing boilerplate code
- **MapStruct** for type-safe object mapping
- **OpenAPI/Swagger** for API documentation
- **JPA/Hibernate** with optimistic locking
- **Comprehensive error handling** with global exception handler
- **Caching** support with Spring Cache abstraction
- **Structured logging** with Logback and JSON formatting
- **Docker Compose** for local development environment
- **JUnit 5** and **Mockito** for testing
- **Checkstyle** for code quality
- **JaCoCo** for code coverage

## 📋 Prerequisites

- JDK 17 or higher
- Docker and Docker Compose
- Gradle 8.x (wrapper included)

## 🛠️ Project Structure

```
backend-application/
├── src/
│   ├── main/
│   │   ├── java/com/example/backend/
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── exception/           # Custom exceptions & handlers
│   │   │   ├── mapper/              # MapStruct mappers
│   │   │   ├── repository/          # JPA repositories
│   │   │   ├── service/             # Business logic layer
│   │   │   └── BackendApplication.java
│   │   └── resources/
│   │       ├── db/migration/        # Flyway migrations
│   │       ├── application.yml      # Main configuration
│   │       ├── application-test.yml # Test configuration
│   │       └── logback-spring.xml   # Logging configuration
│   └── test/
│       └── java/com/example/backend/
│           └── service/             # Unit tests
├── docker/
│   └── mysql/
│       └── init.sql                 # Database initialization
├── config/
│   └── checkstyle/
│       └── checkstyle.xml           # Code style rules
├── build.gradle.kts                 # Gradle build configuration
├── docker-compose.yml               # Docker services
└── README.md
```

## 🚀 Getting Started

### 1. Start the Database

```bash
docker-compose up -d
```

This will start:
- MySQL 8.0 on port 3306
- Redis on port 6379

### 2. Build the Application

```bash
./gradlew build
```

### 3. Run the Application

```bash
./gradlew bootRun
```

The application will be available at: `http://localhost:8080`

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## 🔍 Available Endpoints

### Products API (`/api/v1/products`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/products` | Get all products (paginated) |
| GET | `/api/v1/products/{id}` | Get product by ID |
| GET | `/api/v1/products/sku/{sku}` | Get product by SKU |
| POST | `/api/v1/products` | Create new product |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |
| GET | `/api/v1/products/search?keyword={keyword}` | Search products |
| GET | `/api/v1/products/category/{category}` | Get products by category |
| GET | `/api/v1/products/low-stock?threshold={threshold}` | Get low stock products |

### Health & Monitoring

- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Tests with Coverage Report

```bash
./gradlew test jacocoTestReport
```

Coverage report will be available at: `build/reports/jacoco/test/html/index.html`

### Run Checkstyle

```bash
./gradlew checkstyleMain checkstyleTest
```

## 🏗️ Best Practices Implemented

### 1. **Layered Architecture**
- Clear separation: Controller → Service → Repository
- DTOs for API layer, Entities for persistence layer
- MapStruct for automatic mapping between layers

### 2. **Builder Pattern with Lombok**
```java
Product product = Product.builder()
    .name("Laptop")
    .sku("LAP-001")
    .price(BigDecimal.valueOf(999.99))
    .quantity(100)
    .build();
```

### 3. **Comprehensive Validation**
- Bean Validation annotations (`@NotBlank`, `@Min`, `@Pattern`, etc.)
- Global exception handler for consistent error responses
- Custom exceptions for business logic errors

### 4. **Database Best Practices**
- Flyway migrations for version control
- Optimistic locking with `@Version`
- Proper indexing on frequently queried columns
- HikariCP for high-performance connection pooling

### 5. **Caching Strategy**
- Spring Cache abstraction
- Cache eviction on data modification
- Ready for Redis integration

### 6. **Logging**
- SLF4J with Logback
- JSON format logging for production
- Structured logging with MDC support

### 7. **API Documentation**
- OpenAPI 3.0 specification
- Swagger UI for interactive testing
- Comprehensive annotations on endpoints

### 8. **Testing**
- Unit tests with JUnit 5 and Mockito
- AssertJ for fluent assertions
- High code coverage with meaningful tests
- Test containers support for integration tests

## 🔧 Configuration

### Database Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/backend_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: backend_user
    password: backend_password
```

### Application Properties

Key configuration properties:

- `server.port`: Application port (default: 8080)
- `spring.jpa.hibernate.ddl-auto`: Hibernate DDL mode (validate/update/create)
- `logging.level.*`: Logging levels per package
- `management.endpoints.web.exposure.include`: Actuator endpoints

## 🐳 Docker Support

### Build Docker Image

```bash
./gradlew bootBuildImage
```

### Run with Docker Compose

The provided `docker-compose.yml` includes:
- MySQL 8.0 database
- Redis cache server
- Health checks
- Persistent volumes

## 📊 Monitoring & Observability

### Actuator Endpoints

- `/actuator/health` - Health status
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Logging

Logs are written to:
- Console (human-readable format)
- `logs/application.log` (rolling file)
- `logs/application-json.log` (JSON format for log aggregation)

## 🎯 Example API Requests

### Create a Product

```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "sku": "LAP-GAME-001",
    "price": 1499.99,
    "quantity": 50,
    "category": "Electronics"
  }'
```

### Get All Products

```bash
curl http://localhost:8080/api/v1/products?page=0&size=10&sortBy=name&sortDir=asc
```

### Search Products

```bash
curl http://localhost:8080/api/v1/products/search?keyword=laptop&page=0&size=10
```

## 🔐 Security Considerations

For production deployment, consider adding:
- Spring Security for authentication/authorization
- JWT tokens for stateless authentication
- Rate limiting
- CORS configuration
- HTTPS/TLS
- Secret management (AWS Secrets Manager, HashiCorp Vault)

## 📈 Scalability Features

- **Connection Pooling**: HikariCP for efficient database connections
- **Caching**: Ready for distributed caching with Redis
- **Pagination**: All list endpoints support pagination
- **Batch Operations**: Hibernate batch inserts/updates configured
- **Async Processing**: Ready for `@Async` methods
- **Database Indexing**: Proper indexes on frequently queried columns

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📝 License

This project is licensed under the Apache 2.0 License.

## 🙏 Acknowledgments

- Spring Boot Team for the excellent framework
- Lombok Project for reducing boilerplate
- MapStruct for type-safe mapping
- All open-source contributors

---

**Built with ❤️ using Spring Boot and Best Practices**
