package com.bobo.storage.web.api.request;

import com.bobo.semantic.IntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests that {@code Request(s)} are mapped as I expect by the Jackson {@link ObjectMapper}, when
 * using the (default) autoconfiguration provided by Spring.
 */
@IntegrationTest({ObjectMapper.class, JacksonAutoConfiguration.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class RequestIT {

  private final ObjectMapper mapper;

  @Autowired
  RequestIT(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * @see Jdk8Module
   */
  @Nested
  class Optionals {

    @Test
    void absentProperty() {
      Map<String, String> payload = Map.of("key", "value");
      RequestWithOptional request = mapper.convertValue(payload, RequestWithOptional.class);
      Assertions.assertFalse(request.optionalProperty().isPresent());
    }

    @Test
    void presentProperty() {
      Map<String, String> payload = Map.of("optionalProperty", "value");
      RequestWithOptional request = mapper.convertValue(payload, RequestWithOptional.class);
      Assertions.assertTrue(request.optionalProperty().isPresent());
    }

    public record RequestWithOptional(Optional<String> optionalProperty, String requiredProperty) {}
  }
}
