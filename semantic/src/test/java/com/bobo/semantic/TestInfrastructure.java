package com.bobo.semantic;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Containerised infrastructure for testing.
 *
 * @implNote The containers defined here should be synced with those defined in the {@code
 *     compose.yaml}.
 * @see Testcontainers
 */
public abstract class TestInfrastructure {

  /**
   *
   *
   * {@snippet java:
   *     @Container
   *     @ServiceConnection
   *     private static final JdbcDatabaseContainer<?> database = TestInfrastructure.getDatabase();
   * }
   *
   * @see Container
   * @see ServiceConnection
   */
  public static JdbcDatabaseContainer<?> getDatabase() {
    return new PostgreSQLContainer("postgres:14.12-bullseye");
  }
}
