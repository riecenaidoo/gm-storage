package com.bobo.storage.core.song;

import static com.bobo.storage.core.semantic.AccessForTesting.Modifier;
import static com.bobo.storage.core.semantic.Normalisations.nullIf;
import static com.bobo.storage.core.semantic.Normalisations.skipIfNull;
import static com.bobo.storage.core.semantic.Normalisations.truncateToSize;

import com.bobo.storage.core.semantic.AccessForTesting;
import com.bobo.storage.core.semantic.DomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * The atomic unit of the domain.
 *
 * <p>Represents a reference to a song available on the internet.
 *
 * <p>Encapsulates metadata about the song as well as the current status of this reference.
 */
@Entity
public class Song extends DomainEntity {

  private static final Logger log = LoggerFactory.getLogger(Song.class);

  /**
   * The reference to the song.
   *
   * <p>Modeled as the Uniform Resource Locator (URL) pointing to the song on the internet.
   *
   * <p>Serves as the unique identifier for a {@code Song} entity.
   *
   * @see #toUrl()
   * @see #toUri()
   */
  @Column(unique = true, nullable = false, length = 2048)
  private String url;

  /**
   * The last time this reference was looked up.
   *
   * <p>For a new entity, this is expected to be {@code null} since a {@link Song} is uniquely
   * identified by its reference.
   */
  @SuppressWarnings("unused")
  private LocalDateTime lastLookup;

  /**
   * The normalisation pipeline for {@link Song} metadata.
   *
   * <p>These fields are expected to be relatively short strings. If they exceed 256 characters, we
   * do not throw an exception, because we can safely handle this via truncation. We consider this
   * safe because metadata fields are derived from the {@link #url}.
   *
   * @implNote TODO [design] Consider extracting metadata into a dedicated value object. This may
   *     clarify the role of a Provider. A Provider provides SongMetadata. It would also support
   *     future iterations where we compare metadata across references to determine whether they
   *     refer to the same song.
   */
  private static final Function<String, String> metadataNormalisation =
      nullIf(String::isBlank)
          .andThen(skipIfNull(String::trim))
          .andThen(skipIfNull(truncateToSize(256)));

  /**
   * The title of the song.
   *
   * <p>Part of the metadata associated with this reference.
   */
  @Column(length = 256)
  private String title;

  /**
   * The name of the artist.
   *
   * <p>Part of the metadata associated with this reference.
   */
  @Column(length = 256)
  private String artist;

  /**
   * A reference to artwork for the song.
   *
   * <p>Represented as a URL; part of the metadata associated with this reference.
   */
  @Column(length = 2048)
  private String thumbnailUrl;

  /**
   * @see DomainEntity#DomainEntity()
   */
  protected Song() {}

  /**
   * Creates a reference to a song on the internet.
   *
   * @param url the reference to the song. A valid URL.
   * @see #url
   * @see Song#validatedUrl(String)
   * @throws IllegalArgumentException on an invalid URL.
   */
  public Song(String url) throws IllegalArgumentException {
    this.url = validatedUrl(url);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Song song = (Song) o;
    return Objects.equals(url, song.getUrl());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(url);
  }

  /**
   * @see #url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Convert this reference to a {@link URL}.
   *
   * @return a {@link URL} representing the reference
   * @see #url
   */
  public URL toUrl() {
    try {
      return toUri().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalStateException(
          """
          The URL of a Song is guaranteed to be a valid URL,\s
          but it failed to be parsed into one.\s
          Was this %s created directly into the database,
          circumventing application logic,\s
          or has the application logic failed to guarantee the invariant?\
          """
              .formatted(this.log()),
          e);
    }
  }

  /**
   * Convert this reference to a {@link URI}, as used for constructing network requests.
   *
   * @return a {@link URI} representing the reference
   * @see #url
   */
  public URI toUri() {
    try {
      return URI.create(url);
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException(
          """
          The URL of a Song is guaranteed to be a valid URI and URL,\s
          conforming to RFC 2396, but it failed to be converted back into a URI.\s
          Was this %s created directly into the database,
          circumventing application logic,\s
          or has the application logic failed to guarantee the invariant?\
          """
              .formatted(this.log()),
          e);
    }
  }

  /**
   * Validates and normalizes the given {@code url} to conform to standard URL syntax.
   *
   * <p>All URLs are URIs, but not all URIs are URLs. A URI identifies a resource; a URL specifies a
   * means of locating it, typically including a scheme (like {@code https}) and authority (such as
   * a hostname).
   *
   * @param rawUrl the raw input string representing a URL
   * @return the validated and normalized URL
   * @throws IllegalArgumentException if the input cannot be interpreted as a valid URL
   * @see <a href="https://www.ietf.org/rfc/rfc2396.txt">RFC 2396: Uniform Resource Identifiers
   *     (URI): Generic Syntax</a>
   * @see <a href="https://www.ietf.org/rfc/rfc2732.txt">RFC 2732: Format for Literal IPv6 Addresses
   *     in URLs</a>
   * @see #url
   * @implNote URLs beyond {@code 2048} characters are rejected. While there are no formal
   *     restrictions around URL length, the upper limit is typically {@code 2000} - anything larger
   *     is likely malicious.
   */
  private static String validatedUrl(String rawUrl) throws IllegalArgumentException {
    rawUrl = rawUrl.trim();
    if (rawUrl.isEmpty()) {
      throw new IllegalArgumentException("URL cannot be empty.");
    }
    if (rawUrl.length() > 2048) {
      throw new IllegalArgumentException(
          """
          URL rejected. Exceeds length restriction of 2048 chars. \
          Actual length: %d characters.\
          """
              .formatted(rawUrl.length()));
    }
    try {
      URI uri = new URI(rawUrl); // Syntax-level URI validation (RFC 2396)
      URL url = uri.toURL(); // Ensures absolute URL with valid scheme/host
      return url.toString();
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          """
          URL validation failed. Not an absolute URL. \
          Cannot be polled : %s\
          """
              .formatted(rawUrl),
          e);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(
          "URL validation failed. [RFC2396] Syntax issue with: %s".formatted(rawUrl), e);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(
          ("""
          URL validation failed. Syntactically correct URI,
          but is not a valid URL (likely due to missing scheme or authority) : %s\
          """)
              .formatted(rawUrl),
          e);
    }
  }

  /**
   * Updates the reference (URL) of this song.
   *
   * @param url the new reference URL to assign to this song
   * @implNote Changing the reference is equivalent to creating a new {@code Song}. As a result,
   *     this method resets {@code lastLookup} to {@code null} to indicate that the new reference
   *     requires verification.
   *     <p>The only valid case for mutating the reference is to follow {@code 3xx Redirection}
   *     responses from the host of the URL.
   * @see #url
   * @see #lastLookup
   */
  @AccessForTesting(Modifier.PACKAGE_PRIVATE)
  void setUrl(String url) {
    url = validatedUrl(url);
    if (url.equals(this.url)) return;
    this.url = url;
    this.lastLookup = null;
  }

  /**
   * Performs a <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Methods/HEAD">{@code
   * HEAD}</a> request on this {@link Song}'s {@link #url} to verify its existence.
   *
   * <p>This operation is considered a lookup and will update {@link #lastLookup}. It is intended to
   * be invoked as part of regular lookup cycles i.e. polling.
   *
   * <p>It is not required to be called upon creation, based on the assumption that the client
   * supplying this reference has already verified its existence at the time of submission.
   *
   * @param client the HTTP client to use; must not have a base URL configured.
   * @return the {@link HttpStatusCode} of the {@code HEAD} request.
   *     <p>
   *     <ul>
   *       <li>{@link HttpStatusCode#is3xxRedirection()} requires co-ordination to ensure the new
   *           location ({@link #url}) of this {@link Song} is not already mapped by another {@link
   *           Song}.
   *       <li>{@code 404} should attempt to migrate referrals to another {@link Song} with matching
   *           metadata.
   *     </ul>
   *
   * @implNote Obeys {@code 3xx Redirection} responses by updating the {@link #url} to the
   *     redirection location, if applicable. The new location ({@link #url}) is not immediately
   *     looked-up, because hosts may redirect multiple times. Since the time to complete the entire
   *     redirection chain is unpredictable, further resolution is deferred to subsequent lookup
   *     cycles.
   * @see #url
   * @see #lastLookup
   */
  HttpStatusCode poll(WebClient client) {
    URI uri = toUri();
    ClientResponse response =
        client
            .head()
            .uri(uri)
            .exchangeToMono(Mono::just)
            .block(Duration.ofSeconds(5)); // TODO allow configuration
    assert response != null : "exchangeToMono(Mono::just) never null";
    lookedUp();
    if (response.statusCode().is3xxRedirection()) {
      URI redirectionLocation = response.headers().asHttpHeaders().getLocation();
      if (redirectionLocation != null) {
        setUrl(redirectionLocation.toString());
      } else {
        log.error(
            """
            Verification: Encountered {} response. Unsupported.
            Cannot resolve redirection for {} without a Location header.\
            """,
            response.statusCode(),
            this.log());
      }
    }
    return response.statusCode();
  }

  /**
   * @see #lastLookup
   */
  @AccessForTesting(Modifier.PACKAGE_PRIVATE)
  LocalDateTime getLastLookup() {
    return lastLookup;
  }

  /**
   * @param lastLookup nullable.
   * @see #lastLookup
   */
  @AccessForTesting(Modifier.PACKAGE_PRIVATE)
  void setLastLookup(LocalDateTime lastLookup) {
    this.lastLookup = lastLookup;
  }

  /**
   * Called when the {@code Song} has been looked up.
   *
   * <p>Sets the {@code lastLookup} of the {@code Song} to {@link LocalDate#now()}.
   *
   * @see #lastLookup
   */
  public void lookedUp() {
    this.lastLookup = LocalDateTime.now();
  }

  /**
   * @see #title
   */
  public Optional<String> getTitle() {
    return Optional.ofNullable(title);
  }

  /**
   * @param title nullable. As a blank {@code String} provides no metadata about the {@code Song},
   *     it is treated as {@code null}. Truncates to 256 characters.
   * @see #title
   */
  public void setTitle(String title) {
    this.title = metadataNormalisation.apply(title);
  }

  /**
   * @see #artist
   */
  public Optional<String> getArtist() {
    return Optional.ofNullable(artist);
  }

  /**
   * @param artist nullable. As a blank {@code String} provides no metadata about the {@code Song},
   *     it is treated as {@code null}. Truncates to 256 characters.
   * @see #artist
   */
  public void setArtist(String artist) {
    this.artist = metadataNormalisation.apply(artist);
  }

  /**
   * @see #thumbnailUrl
   */
  public Optional<String> getThumbnailUrl() {
    return Optional.ofNullable(thumbnailUrl);
  }

  /**
   * @param thumbnailUrl a valid URL. Nullable.
   * @see #thumbnailUrl
   * @see Song#validatedUrl(String)
   * @throws IllegalArgumentException on an invalid URL.
   */
  public void setThumbnailUrl(String thumbnailUrl) throws IllegalArgumentException {
    this.thumbnailUrl = Objects.isNull(thumbnailUrl) ? null : validatedUrl(thumbnailUrl);
  }
}
