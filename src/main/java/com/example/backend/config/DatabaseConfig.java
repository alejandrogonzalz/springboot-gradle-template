package com.example.backend.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Database configuration with HikariCP connection pooling.
 *
 * <p>HikariCP is a high-performance JDBC connection pool that provides better performance and
 * reliability compared to other connection pools.
 */
@Configuration
public class DatabaseConfig {

  @Value("${spring.datasource.url}")
  private String url;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${spring.datasource.hikari.maximum-pool-size:10}")
  private int maximumPoolSize;

  @Value("${spring.datasource.hikari.minimum-idle:5}")
  private int minimumIdle;

  @Value("${spring.datasource.hikari.connection-timeout:30000}")
  private long connectionTimeout;

  @Value("${spring.datasource.hikari.idle-timeout:600000}")
  private long idleTimeout;

  @Value("${spring.datasource.hikari.max-lifetime:1800000}")
  private long maxLifetime;

  @Bean
  @Primary
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(username);
    config.setPassword(password);
    config.setMaximumPoolSize(maximumPoolSize);
    config.setMinimumIdle(minimumIdle);
    config.setConnectionTimeout(connectionTimeout);
    config.setIdleTimeout(idleTimeout);
    config.setMaxLifetime(maxLifetime);
    config.setAutoCommit(false);
    config.setConnectionTestQuery("SELECT 1");

    // MySQL optimizations
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "250");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    config.addDataSourceProperty("useServerPrepStmts", "true");
    config.addDataSourceProperty("useLocalSessionState", "true");
    config.addDataSourceProperty("rewriteBatchedStatements", "true");
    config.addDataSourceProperty("cacheResultSetMetadata", "true");
    config.addDataSourceProperty("cacheServerConfiguration", "true");
    config.addDataSourceProperty("elideSetAutoCommits", "true");
    config.addDataSourceProperty("maintainTimeStats", "false");

    return new HikariDataSource(config);
  }
}
