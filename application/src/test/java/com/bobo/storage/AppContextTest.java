package com.bobo.storage;

import com.bobo.semantic.SmokeTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SmokeTest
@ActiveProfiles("test")
@SpringBootTest(classes = App.class)
class AppContextTest {

  private static final Logger log = LoggerFactory.getLogger(AppContextTest.class);

  @Test
  void contextLoads() {
    log.info("Application module context loads successfully.");
  }
}
