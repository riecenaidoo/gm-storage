package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.AccessForTesting;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
  YOUTUBE("https://www.youtube.com/oembed"),
  DEEZER("https://api.deezer.com/oembed"),
  SPOTIFY("https://open.spotify.com/oembed");

  private static final Logger log = LoggerFactory.getLogger(Provider.class);

  /**
   * An {@code endpoint} exposed by the {@code host} that adheres to the {@code oEmbed}
   * specification.
   */
  private final URL endpoint;

  Provider(String endpoint) {
    try {
      this.endpoint = URI.create(endpoint).toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException("Check the endpoint argument, there is probably a mis-input.", e);
    }
  }

  @AccessForTesting(AccessForTesting.Modifier.PACKAGE_PRIVATE)
  URL getEndpoint() {
    return endpoint;
  }

  @AccessForTesting(AccessForTesting.Modifier.PACKAGE_PRIVATE)
  URL getQuery(URL url) {
    try {
      return URI.create(endpoint + "?url=" + url + "&format=json").toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(
          "The instantiation of the oEmbed query, given two valid URLs, was not expected to fail.",
          e);
    }
  }

  /**
   * Looks up metadata for a Song, if data is found the Song will be mutated with the information
   * found.
   *
   * @return true if information about the Song was found.
   */
  static boolean lookup(Song song, WebClient webClient) {
    URL url = song.toUrl();
    for (Provider provider : values()) {
      Optional<OEmbedResponse> metadata = provider.queryProvider(url, webClient);
      song.lookedUp(); // TODO [design] Should this be repeated anytime we lookup the Song, or done
      // once at the root?
      if (metadata.isPresent()) {
        metadata.get().accept(song);
        log.info("Provider#Lookup: Hit. {} provided by {}.", song.log(), provider.name());
        return true;
      }
    }
    log.debug("Provider#Lookup: No hits for {}.", song.log());
    return false;
  }

  private Optional<OEmbedResponse> queryProvider(URL url, WebClient webClient) {
    URL query = getQuery(url);
    URI queryURI;
    try {
      queryURI = query.toURI();
    } catch (URISyntaxException e) {
      // TODO [design] Should probably question whether #getQuery should return a URL or URI then.
      throw new RuntimeException();
    }

    return webClient
        .get()
        .uri(queryURI)
        .accept(MediaType.APPLICATION_JSON)
        .exchangeToMono(Provider::toMono)
        .blockOptional();
  }

  /**
   * Extracted for method referencing.
   *
   * @param response from an oEmbed API.
   * @return a {@link Mono} of an {@link OEmbedResponse}, if the API responded with {@code 200 OK}
   *     otherwise {@link Mono#empty()}.
   */
  private static Mono<OEmbedResponse> toMono(ClientResponse response) {
    return (response.statusCode().equals(HttpStatus.OK))
        ? response.bodyToMono(OEmbedResponse.class)
        : Mono.empty();
  }

  /**
   * @param type The resource type. For this use case we only care about validating the existence
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
   * @param thumbnailHeight The width of the optional thumbnail. If this parameter is present,
   *     {@code thumbnail_url} and {@code thumbnail_height} must also be present.
   * @param thumbnailWidth The height of the optional thumbnail. If this parameter is present,
   *     {@code thumbnail_url} and {@code thumbnail_width} must also be present.
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
      @JsonProperty("cache_age") Optional<String> cacheAge,
      @JsonProperty("thumbnail_url") Optional<String> thumbnailUrl,
      @JsonProperty("thumbnail_height") Optional<String> thumbnailHeight,
      @JsonProperty("thumbnail_width") Optional<String> thumbnailWidth)
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
