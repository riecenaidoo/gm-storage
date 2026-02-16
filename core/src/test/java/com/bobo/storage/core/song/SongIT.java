package com.bobo.storage.core.song;

import com.bobo.semantic.IntegrationTest;
import com.bobo.storage.core.CoreTestConfig;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClient;

/**
 * Testing {@code Song} integrations with the web. Makes live calls on testing URLs to verify things
 * work as expected in the wild.
 *
 * @see ImportAutoConfiguration
 */
@IntegrationTest({Song.class, RestClient.class})
@Tag(IntegrationTest.EXTERNAL_TAG)
@ExtendWith(SpringExtension.class)
@Import(CoreTestConfig.class)
class SongIT {

  private final RestClient client;

  @Autowired
  SongIT(RestClient client) {
    this.client = client;
  }

  /**
   * @see Song#poll(RestClient)
   */
  @DisplayName(
      "Verifying a URL that requests a client to redirect (status code 3xx) should update the"
          + " Song's url to use the target location.")
  @ParameterizedTest
  @MethodSource("redirectingUrls")
  void poll(RedirectingURL redirectingUrl) throws IOException {
    Song song = new Song(redirectingUrl.url);
    Assertions.assertTrue(song.poll(client).is3xxRedirection());
    Assertions.assertEquals(redirectingUrl.targetLocation(), song.getUrl());
  }

  /**
   * @return known redirecting URLs to test against. Note that these are typically URLs are
   *     typically pointing at a temporary that are going to eventually be removed and requesting
   *     you redirect to the new permanent location - so they would need to be verified. It is
   *     pointless writing a test to validate them, as that is what the method we are testing is
   *     trying to do. We can just do an assumption assertion that these are 3xx requests, however.
   */
  protected static Stream<RedirectingURL> redirectingUrls() {
    return Stream.of(
        new RedirectingURL(
            "https://dzr.page.link/iao9CHJgGQA2gDFn9",
            "https://www.deezer.com/track/597400982?host=1506211002&utm_campaign=clipboard-generic&utm_source=user_sharing&utm_content=track-597400982&deferredFl=1&universal_link=1"));
  }

  /**
   * @param url that responds with {@code 3xx Redirection}.
   * @param targetLocation the {@code location} header the {@code url} is requesting the client use.
   *     When populating this value, do not use a browser that may send additional headers and
   *     cookies. Use {@code curl --head} locally.
   */
  protected record RedirectingURL(String url, String targetLocation) {}
}
