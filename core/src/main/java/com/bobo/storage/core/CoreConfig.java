package com.bobo.storage.core;

import com.bobo.storage.core.song.LookupConfig;
import java.net.http.HttpClient;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
class CoreConfig {

  /** Construct a {@link RestClient} using the network configurations defined for the lookup job. */
  @Bean
  public RestClient restClient(RestClient.Builder builder, LookupConfig lookupConfig) {
    HttpClient httpClient =
        HttpClient.newBuilder().connectTimeout(lookupConfig.networkTimeout()).build();
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(lookupConfig.networkTimeout());

    return builder.requestFactory(factory).build();
  }

  @Bean
  public Executor executor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }
}
