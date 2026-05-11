package com.bobo.storage.web;

import com.bobo.semantic.SmokeTest;
import com.bobo.semantic.TestInfrastructure;
import com.bobo.storage.core.CoreContext;
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
@CoreContext
@WebContext
@SpringBootTest
@Testcontainers
class WebContextTest {

  private static final Logger log =
      LoggerFactory.getLogger(com.bobo.storage.web.WebContextTest.class);

  @Container @ServiceConnection
  private static final JdbcDatabaseContainer<?> database = TestInfrastructure.getDatabase();

  @Test
  void contextLoads() {
    log.info("Web module context loads successfully.");
  }
}
