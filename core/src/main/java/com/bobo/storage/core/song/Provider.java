package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.AccessForTesting;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * The source of an embeddable form of content that adheres to the {@code oEmbed} specification.
 *
 * <p>In this domain - the source of a {@link Song}'s {@code URL}; a {@code Provider} would be the
 * {@code host} of that {@code URL}.
 *
 * <p>The name of each enumeration corresponds to the {@code hostname} of the {@code Provider}.
 *
 * <p>Not all {@code hosts} support {@code oEmbed}, and neither are all {@code oEmbed} {@code
 * Providers} are codified in this enumeration.
 *
 * @see <a href="https://oembed.com/#section7.1">oEmbed > 7.1 Providers</a>
 */
enum Provider {
  YOUTUBE("https://www.youtube.com/oembed", Set.of("youtube.com", "youtu.be")),
  DEEZER("https://api.deezer.com/oembed", Set.of("deezer.com")),
  SPOTIFY("https://open.spotify.com/oembed", Set.of("open.spotify.com"));

  private static final Logger log = LoggerFactory.getLogger(Provider.class);

  /**
   * An {@code endpoint} exposed by the {@code host} that adheres to the {@code oEmbed}
   * specification.
   */
  private final URI endpoint;

  /**
   * Zero of more known hosts of the {@link Provider}.
   *
   * @see <a href="https://developer.mozilla.org/en-US/docs/Web/API/URL/host">URL Host | MDN</a>
   */
  private final Set<String> hosts;

  Provider(String endpoint, Set<String> hosts) {
    this.endpoint = URI.create(endpoint);
    this.hosts = Objects.requireNonNullElse(hosts, Set.of());
  }

  @AccessForTesting(AccessForTesting.Modifier.PACKAGE_PRIVATE)
  URI getEndpoint() {
    return endpoint;
  }

  @AccessForTesting(AccessForTesting.Modifier.PACKAGE_PRIVATE)
  URI getQuery(Song song) {
    return UriComponentsBuilder.fromUri(endpoint)
        .queryParam("url", song.getUrl())
        .queryParam("format", "json")
        .build()
        .toUri();
  }

  /**
   * @return {@code true} if the {@link Song#getUrl()} matches a known URL pattern of the {@link
   *     Provider}.
   */
  public boolean likelyProvides(Song song) {
    URI uri = song.toUri();
    String host = uri.getHost();

    if (host == null) return false;

    return hosts.stream().anyMatch(h -> host.equals(h) || host.endsWith("." + h));
  }

  /**
   * Returns the {@link Provider Providers} whose URL pattern matches the given {@link Song}.
   *
   * <p>If no provider matches, all providers are returned as a fallback and a warning is logged to
   * highlight a potentially new or unsupported URL pattern.
   *
   * @param song the song to evaluate.
   * @return matching providers, or all providers if none match.
   */
  static Provider[] possibleProviders(Song song) {
    Provider[] providers =
        Arrays.stream(Provider.values())
            .filter(provider -> provider.likelyProvides(song))
            .toArray(Provider[]::new);
    if (providers.length == 0) {
      log.warn("Odin: A new pattern has emerged, \"{}\" — who could it belong to?", song.getUrl());
      return Provider.values();
    }
    return providers;
  }

  /**
   * Lookup metadata for a {@link Song}, updating the {@link Song} object if any is found.
   *
   * @return {@code true} if metadata for the {@link Song} was found, {@code false} otherwise.
   */
  static boolean lookup(Song song, RestClient client, Executor executor) {
    Provider[] providers = possibleProviders(song);
    //noinspection unchecked
    CompletableFuture<ResponseEntity<OEmbedResponse>>[] responses =
        new CompletableFuture[providers.length];
    for (int i = 0; i < providers.length; i++) {
      Provider provider = providers[i];
      URI uri = provider.getQuery(song);
      responses[i] = CompletableFuture.supplyAsync(() -> provider.query(uri, client), executor);
    }

    CompletableFuture.allOf(responses).join();
    for (int i = 0; i < responses.length; i++) {
      var response = responses[i].join();
      OEmbedResponse metadata = response.getBody();

      if (response.getStatusCode().is2xxSuccessful() && metadata != null) {
        log.debug("Odin: {} hails from {}.", song.log(), providers[i].name());
        metadata.accept(song);

        return true;
      }
    }

    log.debug("Odin: {} remains obscured to me.", song.log());
    return false;
  }

  /**
   * Extracted from {@link #lookup(Song, RestClient, Executor)} for readability and normalisation.
   */
  private ResponseEntity<OEmbedResponse> query(URI uri, RestClient client) {
    return client
        .get()
        .uri(uri)
        .accept(MediaType.APPLICATION_JSON)
        .exchange(
            (request, response) -> {
              if (response.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.ok(response.bodyTo(OEmbedResponse.class));
              } else {
                return ResponseEntity.notFound().build();
              }
            });
  }

  /**
   * @param type The resource type. For this use case we only care about validating the existence of
   *     the content, and any metadata about it. We may also forward the provider information for
   *     another consumer to retrieve the embedded content.
   * @param version The oEmbed version number. This must be {@code 1.0}.
   * @param title A text title, describing the resource.
   * @param authorName The name of the author/owner of the resource.
   * @param authorUrl A URL for the author/owner of the resource.
   * @param providerName The name of the resource provider.
   * @param providerUrl The url of the resource provider.
   * @param cacheAge The suggested cache lifetime for this resource, in seconds. Consumers may
   *     choose to use this value or not.
   * @param thumbnailUrl A URL to a thumbnail image representing the resource. The thumbnail must
   *     respect any {@code maxwidth} and {@code maxheight} parameters. If this parameter is
   *     present, {@code thumbnail_width} and {@code thumbnail_height} must also be present.
   * @param thumbnailHeight The height of the optional thumbnail. If this parameter is present,
   *     {@code thumbnail_url} and {@code thumbnail_width} must also be present.
   * @param thumbnailWidth The width of the optional thumbnail. If this parameter is present, {@code
   *     thumbnail_url} and {@code thumbnail_height} must also be present.
   * @see <a href="https://oembed.com/#section2.3">2.3.4. Response Parameters</a>
   */
  private record OEmbedResponse(
      String type,
      String version,
      Optional<String> title,
      @JsonProperty("author_name") Optional<String> authorName,
      @JsonProperty("author_url") Optional<String> authorUrl,
      @JsonProperty("provider_name") Optional<String> providerName,
      @JsonProperty("provider_url") Optional<String> providerUrl,
      @JsonProperty("cache_age") Optional<Long> cacheAge,
      @JsonProperty("thumbnail_url") Optional<String> thumbnailUrl,
      @JsonProperty("thumbnail_height") Optional<Integer> thumbnailHeight,
      @JsonProperty("thumbnail_width") Optional<Integer> thumbnailWidth)
      implements Consumer<Song> {

    /**
     * Accepts a Song to apply the metadata in the response to.
     *
     * @param song to apply the metadata to.
     */
    public void accept(Song song) {
      title.ifPresent(song::setTitle);
      authorName.ifPresent(song::setArtist);
      thumbnailUrl.ifPresent(song::setThumbnailUrl);
    }
  }
}
