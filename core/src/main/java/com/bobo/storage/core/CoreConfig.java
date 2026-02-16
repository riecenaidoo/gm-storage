package com.bobo.storage.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class CoreConfig {

  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    return builder.build();
  }
}
