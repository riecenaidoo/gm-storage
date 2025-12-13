package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.CoreService;
import com.bobo.storage.core.semantic.DomainEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

/**
 * Performs {@link Song} lookups, coordinating with other services, where necessary.
 *
 * <p>While each service manages CRUD operations independently, a {@link Song} lookup may trigger a
 * merge between two {@link Song} entities, which requires coordination with any related entities.
 *
 * <p>Merging occurs when:
 *
 * <ul>
 *   <li>The host of the {@code Song} URL responds with a 3xx Redirection request whose location is
 *       a URL already associated with another {@code Song}.
 *   <li>The original URL is permanently unavailable, but a replacement {@code Song}—typically from
 *       a different {@link Provider}—is available.
 * </ul>
 */
@CoreService
public class LookupService {

  private static final Logger log = LoggerFactory.getLogger(LookupService.class);

  private final WebClient webClient;

  private final SongService songs;

  LookupService(WebClient webClient, SongService songs) {
    this.webClient = webClient;
    this.songs = songs;
  }

  /**
   * Performs the lookup of a {@link Song}, which is a two-step process that may require
   * coordination with other {@link DomainEntity}.
   *
   * <ol>
   *   <li>Verify the URL of the {@code Song}.
   *   <li>Look up metadata for the {@code Song} using its URL.
   * </ol>
   *
   * @param song to lookup.
   * @implNote This is the atomic operation. If it fails the batch should not fail, but this
   * individual should be retried.
   * <p>In the event of a redirection, we defer lookup for the next pass. It is very possible to
   * be redirected multiple times. We should only poll Providers on a stable URL.
   * @see Song#poll(WebClient)
   * @see Provider#lookup(Song, WebClient)
   */
  @Transactional
  public void lookup(Song song) {
    try {
      HttpStatusCode statusCode = song.poll(webClient);
      if (statusCode.is2xxSuccessful()) {
        boolean hit = Provider.lookup(song, webClient);
        if (!hit) {
          applyYouTubeResolutions(song);
        }
      } else if (statusCode.is3xxRedirection()) {
        log.debug("Lookup: {} redirects. URL updated. Lookup deferred.", song.log());
      } else if (statusCode.is5xxServerError()) {
        log.info("""
                         Lookup: Host server encountered an exception during routine poll of {}.
                         Will retry later.""", song.log());
      }
      songs.updateSong(song);
    } catch (Exception ex) {
      log.error(
              "Lookup: Exception encountered on {} with url {}",
              song.log(),
              song.getUrl(),
              ex);

      Optional<Song> originalSong = songs.find(song.getId());
      if (originalSong.isPresent()) {
        song = originalSong.get();
        song.lookedUp();
        songs.updateSong(song);
        log.info(
                "Lookup: Gracefully handled exception on {}. Removed from lookup queue.",
                song.log());
      } else {
        log.warn(
                """
                        Lookup: Exception handling failed because {} is no longer
                          present in the repository. Was it removed while lookup was occurring?""",
                song.log());
      }
    }
  }

  /**
   * Attempt to resolve potential issues with YouTube URLs.
   *
   * @implNote YouTube share links add additional share identifier query parameters and URLs of
   * videos inside a mix or playlist will contain query parameters to link to the list. Their oEmbed
   * endpoint does not resolve these URLs as belonging to YouTube.
   */
  private void applyYouTubeResolutions(Song song) {
    if (!song.getUrl().matches(".*(youtube\\.com|youtu\\.be).*")) {
      return;
    }

    URL url = song.toUrl();
    String query = url.getQuery();
    if (query == null) {
      return;
    }

    Optional<String> videoId = Arrays.stream(query.split("&"))
                                     .map(param -> param.split("="))
                                     .filter(keyValue -> keyValue.length == 2 && keyValue[0].equals(
                                             "v"))
                                     .map(keyValue -> keyValue[1])
                                     .findFirst();

    if (videoId.isEmpty()) {
      return;
    }

    log.debug("""
                      Lookup: {} is likely provided by YouTube,
                      but there is an issue resolving metadata for the URL via YouTube.
                      Normalising the URL through video id extraction.""", song.log());
    song.setUrl("https://www.youtube.com/watch?v=" + videoId.get());
  }
}
