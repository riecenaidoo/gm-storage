package com.bobo.storage.web;

import com.bobo.semantic.SmokeTest;
import com.bobo.storage.core.CoreContext;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SmokeTest
@ActiveProfiles("test")
@CoreContext
@WebContext
@SpringBootTest
class WebContextTest {

  private static final Logger log =
      LoggerFactory.getLogger(com.bobo.storage.web.WebContextTest.class);

  @Test
  void contextLoads() {
    log.info("Web module context loads successfully.");
  }
}
