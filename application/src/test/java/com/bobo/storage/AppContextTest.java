package com.bobo.storage;

import com.bobo.semantic.SmokeTest;
import com.bobo.semantic.TestInfrastructure;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SmokeTest
@ActiveProfiles("test")
@SpringBootTest(classes = App.class)
@Testcontainers
class AppContextTest {

  private static final Logger log = LoggerFactory.getLogger(AppContextTest.class);

  @Container @ServiceConnection
  private static final JdbcDatabaseContainer<?> database = TestInfrastructure.getDatabase();

  @Test
  void contextLoads() {
    log.info("Application module context loads successfully.");
  }
}
