package com.bobo.storage.core.song;

import com.bobo.semantic.IntegrationTest;
import com.bobo.storage.core.CoreTestConfig;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClient;

/**
 * Testing Integration with OEmbed Providers.
 *
 * @see ImportAutoConfiguration
 */
@IntegrationTest({Provider.class, RestClient.class})
@Tag(IntegrationTest.EXTERNAL_TAG)
@ExtendWith(SpringExtension.class)
@Import(CoreTestConfig.class)
class ProviderIT {

  private final RestClient client;

  @Autowired
  ProviderIT(RestClient client) {
    this.client = client;
  }

  /**
   * @implNote Spotify is taking > {@code 5s} to respond to a URL that does not belong to it. It is
   *     likely an issue with Spotify not parsing the URL pre-emptively to check if it is one of its
   *     own. This delays tests and application code. We could increase the {@link
   *     LookupConfig#networkTimeout()} to handle this, or build application logic around parsing
   *     which {@link Provider} is likely to host the {@link Song#getUrl()} — which is the route
   *     we've gone for now.
   * @see Provider#lookup(Song, RestClient, Executor)
   */
  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://www.youtube.com/watch?v=rdwz7QiG0lk",
        "https://www.deezer.com/us/track/350027801",
        "https://www.deezer.com/track/350027801?host=0&utm_campaign=clipboard-generic&utm_source=user_sharing&utm_content=track-350027801&deferredFl=1&universal_link=1",
        "https://open.spotify.com/track/6G6EAmGXX7T52zOWj2GWPE?si=145d2b7ff94641dc"
      })
  void lookup(String url) {
    Song song = new Song(url);

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Assertions.assertTrue(Provider.lookup(song, client, executor));
    }
  }
}
