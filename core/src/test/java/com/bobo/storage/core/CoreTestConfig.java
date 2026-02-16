package com.bobo.storage.core;

import com.bobo.storage.core.song.LookupConfig;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 *
 *
 * <pre>{@code
 * @ExtendWith(SpringExtension.class)
 * @Import(CoreTestConfig.class)
 * class SomeIT{
 *     // ...
 * }
 * }</pre>
 */
@ImportAutoConfiguration(classes = {RestClientAutoConfiguration.class})
@Import(CoreConfig.class)
public class CoreTestConfig {

  @Bean
  public LookupConfig lookupConfig() {
    return new LookupConfig();
  }
}
