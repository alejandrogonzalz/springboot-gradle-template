package com.example.backend;

import com.example.backend.user.dto.CreateUserRequest;
import com.example.backend.user.entity.UserRole;
import com.example.backend.user.service.UserService;
import jakarta.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main entry point for the Backend Application.
 *
 * <p>This is a Spring Boot application that provides RESTful APIs for managing products, users, and
 * other business entities.
 */
@Slf4j
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableAspectJAutoProxy
@RequiredArgsConstructor
public class BackendApplication {

  private final Environment env;
  private final DataSource dataSource;
  private final UserService userService;

  public static void main(String[] args) {
    log.info("╔════════════════════════════════════════════════════════════════╗");
    log.info("║          Starting Backend Application...                       ║");
    log.info("╚════════════════════════════════════════════════════════════════╝");

    try {
      ConfigurableApplicationContext context =
          SpringApplication.run(BackendApplication.class, args);
      Environment env = context.getEnvironment();
      logApplicationStartup(env);
    } catch (Exception e) {
      log.error("╔════════════════════════════════════════════════════════════════╗");
      log.error("║          APPLICATION STARTUP FAILED!                           ║");
      log.error("╚════════════════════════════════════════════════════════════════╝");
      log.error("❌ Error: {}", e.getMessage());

      // Provide helpful hints based on error type
      if (e.getMessage() != null) {
        if (e.getMessage().contains("Communications link failure")
            || e.getMessage().contains("Connection refused")) {
          log.error("");
          log.error("💡 DATABASE CONNECTION FAILED!");
          log.error("   Possible causes:");
          log.error("   1. MySQL is not running");
          log.error("   2. Wrong host/port configuration");
          log.error("   3. Database credentials are incorrect");
          log.error("");
          log.error("🔧 Solutions:");
          log.error("   • Start Docker services: docker-compose up -d");
          log.error("   • Check services: docker ps");
          log.error("   • View MySQL logs: docker logs backend-mysql");
          log.error("   • Verify connection: mysql -h localhost -P 3306 -u backend_user -p");
        } else if (e.getMessage().contains("Redis")) {
          log.error("");
          log.error("💡 REDIS CONNECTION FAILED!");
          log.error("   Possible causes:");
          log.error("   1. Redis is not running");
          log.error("   2. Wrong Redis host/port configuration");
          log.error("");
          log.error("🔧 Solutions:");
          log.error("   • Start Docker services: docker-compose up -d");
          log.error("   • Check services: docker ps");
          log.error("   • View Redis logs: docker logs backend-redis");
        }
      }

      System.exit(1);
    }
  }

  @PostConstruct
  public void init() {
    String activeProfiles = String.join(", ", env.getActiveProfiles());
    if (activeProfiles.isEmpty()) {
      activeProfiles = "default";
    }

    log.info("╔════════════════════════════════════════════════════════════════╗");
    log.info("║          Application Configuration                             ║");
    log.info("╚════════════════════════════════════════════════════════════════╝");
    log.info("📋 Active Profile: {}", activeProfiles);
    log.info("☕ Java Version: {}", System.getProperty("java.version"));
    log.info("🏠 Working Directory: {}", System.getProperty("user.dir"));

    // Test database connection
    testDatabaseConnection();

    // Create default admin user
    createDefaultUser();
  }

  private void createDefaultUser() {
    try {
      for (int i = 1; i <= 10; i++) {
        CreateUserRequest request =
            CreateUserRequest.builder()
                .username("user" + i)
                .password("User123!")
                .firstName("User")
                .lastName(String.valueOf(i))
                .email("user" + i + "@example.com")
                .userRole(i == 1 ? UserRole.ADMIN : UserRole.USER)
                .build();

        userService.registerUser(request);
      }
      log.info("✅ 10 default users created successfully (user1-user10)");
    } catch (Exception e) {
      log.warn("⚠️  Default user creation skipped: {}", e.getMessage());
    }
  }

  private void testDatabaseConnection() {
    try {
      log.info("");
      log.info("╔════════════════════════════════════════════════════════════════╗");
      log.info("║          Testing Database Connection...                        ║");
      log.info("╚════════════════════════════════════════════════════════════════╝");

      String dbUrl = env.getProperty("spring.datasource.url", "NOT CONFIGURED");
      String dbUsername = env.getProperty("spring.datasource.username", "NOT CONFIGURED");

      log.info("🔗 Database URL: {}", maskPassword(dbUrl));
      log.info("👤 Database User: {}", dbUsername);

      try (Connection connection = dataSource.getConnection()) {
        String dbProductName = connection.getMetaData().getDatabaseProductName();
        String dbProductVersion = connection.getMetaData().getDatabaseProductVersion();

        log.info("✅ Database connection successful!");
        log.info("   • Product: {} {}", dbProductName, dbProductVersion);
        log.info("   • Catalog: {}", connection.getCatalog());
        log.info("   • Read-only: {}", connection.isReadOnly());
      }
    } catch (Exception e) {
      log.error("❌ Database connection FAILED!");
      log.error("   Error: {}", e.getMessage());
      log.error("");
      log.error("💡 Troubleshooting steps:");
      log.error("   1. Check if Docker services are running: docker ps");
      log.error("   2. Start services if needed: docker-compose up -d");
      log.error("   3. Check MySQL logs: docker logs backend-mysql");
      log.error("   4. Verify port 3306 is available: lsof -i :3306");
      log.error("   5. Test connection manually:");
      log.error("      mysql -h localhost -P 3306 -u backend_user -pbackend_password");

      // Don't fail startup, let Spring handle it
    }
  }

  private static void logApplicationStartup(Environment env) {
    String protocol = "http";
    String serverPort = env.getProperty("server.port", "8080");
    String contextPath = env.getProperty("server.servlet.context-path", "/");
    String hostAddress = "localhost";

    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.warn("⚠️  Unable to determine host address");
    }

    String activeProfiles = String.join(", ", env.getActiveProfiles());
    if (activeProfiles.isEmpty()) {
      activeProfiles = "default";
    }

    log.info("");
    log.info("╔════════════════════════════════════════════════════════════════╗");
    log.info("║          🚀 APPLICATION STARTED SUCCESSFULLY! 🚀               ║");
    log.info("╚════════════════════════════════════════════════════════════════╝");
    log.info("");
    log.info("🌐 Application is running!");
    log.info("   • Local:      {}://localhost:{}{}", protocol, serverPort, contextPath);
    log.info("   • External:   {}://{}:{}{}", protocol, hostAddress, serverPort, contextPath);
    log.info("   • Profile(s): {}", activeProfiles);
    log.info("");
    log.info("📚 API Documentation:");
    log.info("   • Swagger UI: {}://localhost:{}/swagger-ui.html", protocol, serverPort);
    log.info("   • API Docs:   {}://localhost:{}/v3/api-docs", protocol, serverPort);
    log.info("");
    log.info("💊 Health Check:");
    log.info("   • Actuator:   {}://localhost:{}/actuator/health", protocol, serverPort);
    log.info("");
    log.info("🔐 Default User Credentials:");
    log.info("   • Admin: user1 / User123!");
    log.info("   • Users: user2-user10 / User123!");
    log.info("");
    log.info("╔════════════════════════════════════════════════════════════════╗");
    log.info("║          Ready to accept requests!                             ║");
    log.info("╚════════════════════════════════════════════════════════════════╝");
  }

  private String maskPassword(String url) {
    if (url == null) {
      return "NOT CONFIGURED";
    }
    // Mask password in URL if present (e.g., jdbc:mysql://localhost:3306/db?password=secret)
    return url.replaceAll("password=[^&\\s]+", "password=****");
  }
}
