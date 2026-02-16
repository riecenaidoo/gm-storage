package com.bobo.storage.core.song;

import com.bobo.semantic.UnitTest;
import java.time.LocalDateTime;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.reactive.function.client.WebClient;

@UnitTest(Song.class)
class SongTest {

  /*
  TODO: "https://foo" is a valid URL, but not resolvable. It does not specify a top level domain.
  This could be identified by parsing, but the best way is to attempt to resolve the host.
  e.g. `InetAddress.getByName(url.getHost());` - but this is blocking.
  It would also be identified during polling - so we should probably handle this during polling.
   */

  private final Random random = new Random();

  private SongMother mother;

  @BeforeEach
  void setup() {
    mother = new SongMother(random);
  }

  /**
   * @see Song#getUrl()
   * @see Song#toUrl()
   * @see Song#toUri()
   */
  @Nested
  class Url {

    @Test
    void url() {
      // Given
      String url = "https://" + "c".repeat(2048 - "https://".length() - ".com".length()) + ".com";
      // When
      Song song = new Song(url);
      // Then
      Assertions.assertEquals(url, song.getUrl());
      Assertions.assertDoesNotThrow(song::toUri);
      Assertions.assertDoesNotThrow(song::toUrl);
    }

    @Test
    void normalised() {
      // Given
      String url = " ".repeat(1024) + "https://b.com" + " ".repeat(1024);
      // When
      Song song = new Song(url);
      // Then
      Assertions.assertEquals(url.trim(), song.getUrl());
      Assertions.assertDoesNotThrow(song::toUri);
      Assertions.assertDoesNotThrow(song::toUrl);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "ttps://foo.com", "https://", ".com", "bar.com", "foo/bar"})
    void validatedUrl(String givenInvalidUrl) {
      // Then
      Assertions.assertThrows(
          IllegalArgumentException.class,
          // When
          () -> new Song(givenInvalidUrl));
    }

    @Test
    void rejectLongUrls() {
      // Given
      String suspiciouslyLongUrl = "https://" + "c".repeat(2048) + ".com";
      // Then
      Assertions.assertThrows(
          IllegalArgumentException.class,
          // When
          () -> new Song(suspiciouslyLongUrl));
    }
  }

  /**
   * @see Song#lookedUp()
   * @see Song#setUrl(String)
   */
  @Nested
  @DisplayName("LastLookup time is coupled to the URL.")
  class LastLookup {

    @Test
    @DisplayName("If the URL is changed, lastLookup should be cleared.")
    void setUrl() {
      // Given
      Song song = mother.get();
      song.lookedUp();
      // When
      song.setUrl(mother.get().getUrl());
      // Then
      Assertions.assertNull(song.getLastLookup());
    }

    @Test
    @DisplayName("If the URL is unchanged, lastLookup should remain as is.")
    void setSameUrl() {
      // Given
      Song song = mother.get();
      song.lookedUp();
      LocalDateTime lastLookup = song.getLastLookup();
      // When
      song.setUrl(song.getUrl());
      // Then
      Assertions.assertEquals(lastLookup, song.getLastLookup());
    }
  }

  /**
   * @see Song#getTitle()
   * @see Song#setTitle(String)
   */
  @Nested
  class Title {

    @Test
    void title() {
      // Given
      Song song = mother.get();
      String title = "Same Soul";
      title += "c".repeat(256 - title.length());
      // When
      song.setTitle(title);
      // Then
      Assertions.assertTrue(song.getTitle().isPresent());
      Assertions.assertEquals(title, song.getTitle().get());
    }

    @Test
    @DisplayName("Metadata may not be available; it is nullable.")
    void nullable() {
      // Given
      Song song = mother.get();
      // When
      song.setTitle(null);
      // Then
      Assertions.assertTrue(song.getTitle().isEmpty());
    }

    @Test
    void normalised() {
      // Given
      Song song = mother.get();
      String title = " ".repeat(128) + "Same Soul" + " ".repeat(128);
      // When
      song.setTitle(title);
      // Then
      Assertions.assertTrue(song.getTitle().isPresent());
      Assertions.assertEquals(title.trim(), song.getTitle().get());
    }

    @Test
    @DisplayName("A blank string provides no actionable metadata; it is considered null.")
    void blankIsNull() {
      // Given
      Song song = mother.get();
      // When
      song.setTitle(" ");
      // Then
      Assertions.assertTrue(song.getTitle().isEmpty());
    }

    @Test
    void truncates() {
      // Given
      Song song = mother.get();
      // When
      song.setTitle("c".repeat(257));
      // Then
      Assertions.assertTrue(song.getTitle().isPresent());
      Assertions.assertEquals(256, song.getTitle().get().length());
    }
  }

  /**
   * @see Song#getArtist()
   * @see Song#setArtist(String)
   */
  @Nested
  class Artist {

    @Test
    void artist() {
      // Given
      Song song = mother.get();
      String artist = "Lynn Gunn";
      artist += "c".repeat(256 - artist.length());
      // When
      song.setArtist(artist);
      // Then
      Assertions.assertTrue(song.getArtist().isPresent());
      Assertions.assertEquals(artist, song.getArtist().get());
    }

    @Test
    @DisplayName("Metadata may not be available; it is nullable.")
    void nullable() {
      // Given
      Song song = mother.get();
      // Then
      Assertions.assertDoesNotThrow(() -> song.setArtist(null));
      Assertions.assertTrue(song.getArtist().isEmpty());
    }

    @Test
    void normalised() {
      // Given
      Song song = mother.get();
      String artist = " ".repeat(128) + "Lynn Gunn" + " ".repeat(128);
      song.setArtist(artist);
      // Then
      Assertions.assertTrue(song.getArtist().isPresent());
      Assertions.assertEquals(artist.trim(), song.getArtist().get());
    }

    @Test
    @DisplayName("A blank string provides no actionable metadata; it is considered null.")
    void blankIsNull() {
      // Given
      Song song = mother.get();
      // Then
      Assertions.assertDoesNotThrow(() -> song.setArtist(" "));
      Assertions.assertTrue(song.getArtist().isEmpty());
    }

    @Test
    void truncates() {
      // Given
      Song song = mother.get();
      // When
      song.setArtist("c".repeat(257));
      // Then
      Assertions.assertTrue(song.getArtist().isPresent());
      Assertions.assertEquals(256, song.getArtist().get().length());
    }
  }

  /**
   * @see Song#getThumbnailUrl()
   * @see Song#setThumbnailUrl(String)
   */
  @Nested
  class ThumbnailUrl {

    @Test
    void thumbnailUrl() {
      // Given
      Song song = mother.get();
      String url = "https://" + "c".repeat(2048 - "https://".length() - ".com".length()) + ".com";
      // When
      song.setThumbnailUrl(url);
      // Then
      Assertions.assertTrue(song.getThumbnailUrl().isPresent());
      Assertions.assertEquals(url, song.getThumbnailUrl().get());
    }

    @Test
    @DisplayName("Metadata may not be available; it is nullable.")
    void nullable() {
      // Given
      Song song = mother.get();
      // When
      song.setThumbnailUrl(null);
      // Then
      Assertions.assertTrue(song.getThumbnailUrl().isEmpty());
    }

    @Test
    void normalised() {
      // Given
      Song song = mother.get();
      String url = " ".repeat(1024) + "https://b.com" + " ".repeat(1024);
      // When
      song.setThumbnailUrl(url);
      // Then
      Assertions.assertTrue(song.getThumbnailUrl().isPresent());
      Assertions.assertEquals(url.trim(), song.getThumbnailUrl().get());
    }

    @ParameterizedTest
    @DisplayName("If a thumbnail is provided, it must be a valid URL.")
    @ValueSource(strings = {"", " ", "ttps://foo.com", "https://", ".com", "bar.com", "foo/bar"})
    void validatedUrl(String givenInvalidUrl) {
      // Given
      Song song = mother.get();
      // Then
      Assertions.assertThrows(
          IllegalArgumentException.class,
          // When
          () -> song.setThumbnailUrl(givenInvalidUrl));
    }

    @Test
    void rejectLongUrls() {
      // Given
      Song song = mother.get();
      String suspiciouslyLongUrl = "https://" + "c".repeat(2048) + ".com";
      // Then
      Assertions.assertThrows(
          IllegalArgumentException.class,
          // When
          () -> song.setThumbnailUrl(suspiciouslyLongUrl));
    }
  }

  /**
   * @see Song#equals(Object)
   * @see Object#equals(Object)
   */
  @Nested
  class Equals {

    @Test
    void basedOnUrl() {
      // Given
      Song song = new Song("https://a.com");
      // Then
      Assertions.assertEquals(song, new Song(song.getUrl()));
      Assertions.assertNotEquals(new Song("https://b.com"), song);
    }

    @Test
    @SuppressWarnings({"EqualsWithItself"})
    void reflexive() {
      // Given
      Song song = mother.get();
      // Then
      Assertions.assertEquals(song, song);
    }

    @Test
    void symmetric() {
      // Given
      Song x = mother.get();
      Song y = mother.withUrls(x::getUrl).get();
      // Then
      Assertions.assertEquals(x, y, "Test assumption failed.");
      Assertions.assertEquals(y, x);
    }

    @Test
    void transitive() {
      // Given
      Song x = mother.get();
      mother.withUrls(x::getUrl);
      Song y = mother.get();
      Song z = mother.get();
      // Then
      Assertions.assertEquals(x, y, "Test assumption failed.");
      Assertions.assertEquals(y, z, "Test assumption failed.");
      Assertions.assertEquals(x, z);
    }

    @Test
    void consistent() {
      // Given
      Song x = mother.get();
      Song y = mother.get();
      mother.withUrls(x::getUrl);
      Song z = mother.get();
      // Then
      for (int i = 0; i < 50; i++) {
        Assertions.assertNotEquals(x, y);
        Assertions.assertEquals(x, z);
      }
    }

    @Test
    void nullInequality() {
      // Given
      Song song = mother.get();
      // Then
      //noinspection MisorderedAssertEqualsArguments  // Contract testing
      Assertions.assertNotEquals(song, null);
    }

    @Test
    void otherClass() {
      // Given
      Song song = mother.get();
      // Then
      //noinspection MisorderedAssertEqualsArguments  // Contract testing
      Assertions.assertNotEquals(song, new Object());
    }
  }

  /**
   * @see Song#hashCode()
   * @see Object#hashCode()
   */
  @Nested
  class HashCode {

    @Test
    void consistent() {
      // Given
      Song song = mother.get();
      int hashCode = song.hashCode();
      // Then
      for (int i = 0; i < 50; i++) {
        Assertions.assertEquals(
            hashCode,
            song.hashCode(),
            """
            The hashCode method must consistently return the same integer, \
            provided no information used in equals comparisons on the object is modified.\
            """);
      }
    }

    @Test
    void equality() {
      // Given
      Song x = mother.get();
      Song y = mother.withUrls(x::getUrl).get();
      // Then
      Assertions.assertEquals(x, y, "Test assumption failed.");
      Assertions.assertEquals(x.hashCode(), y.hashCode());
    }
  }

  /**
   * @see Song#poll(WebClient)
   * @see SongIT#poll(SongIT.RedirectingURL)
   * @implNote Functionality verified by the IT, but correctness and expectations should still be
   *     tested.
   */
  @Nested
  @Disabled("TODO: Learn how to mock/test with WebClient")
  class Poll {}
}
