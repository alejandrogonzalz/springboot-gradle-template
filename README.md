# Backend Application - Spring Boot Template

A production-ready Spring Boot backend application template demonstrating best practices for building scalable monolithic applications.

## 🚀 Features

- **Spring Boot 3.2.0** with Java 21
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

- JDK 21
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
./gradlew bootRun --args='--spring.profiles.active=local'
```

The application will be available at: `http://localhost:8080`

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

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

---
