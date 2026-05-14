package com.bobo.storage.core.song;

import com.bobo.semantic.UnitTest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UnitTest(Provider.class)
class ProviderTest {

  /**
   * The statement
   *
   * <pre>
   *   {@code URI.create(endpoint)}
   * </pre>
   *
   * in the constructor , throws a {@code RuntimeException}.
   *
   * <p>If there is a problem with the syntax of any provider's {@code endpoint} it will fail at
   * runtime, so this test exists solely to check if the {@code Provider} enumeration can be
   * instantiated without exceptions. If it can, the endpoints used are well-formed URLs.
   *
   * <p>I've done this as a {@code BeforeAll} because if this precondition fails, no other test
   * cases could ever execute.
   *
   * @see Provider#Provider(String, Set)
   */
  @SuppressWarnings("JavadocReference")
  @BeforeAll
  static void enumerationEndpointsAreWellFormedUrls() {
    Assertions.assertDoesNotThrow(Provider::values, "Provider enumeration failed to instantiate.");
  }

  @Test
  void providersHaveDistinctEndpoints() {
    int providerCount = Provider.values().length;
    int distinctEndpoints =
        Arrays.stream(Provider.values())
            .map(Provider::getEndpoint)
            .collect(Collectors.toSet())
            .size();
    Assertions.assertEquals(
        providerCount, distinctEndpoints, "There is a duplication in endpoints.");
  }

  /**
   * @see Provider#getQuery(Song)
   */
  @DisplayName("Providers can create the oEmbed query")
  @Test
  void getQuery() {
    Song song = new SongMother().withUrls().get();
    for (Provider provider : Provider.values()) {
      Assertions.assertDoesNotThrow(
          () -> provider.getQuery(song),
          "%s enumeration fails to create the oEmbed query from a valid URL."
              .formatted(provider.name()));
    }
  }

  @Test
  void likelyProvides() {
    Assertions.assertTrue(
        Provider.YOUTUBE.likelyProvides(new Song("https://www.youtube.com/watch?v=rdwz7QiG0lk")));
  }
}
