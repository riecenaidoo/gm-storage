package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.CoreService;
import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.song.SongLookup.Status;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

/**
 * Lookup a {@link Song} to verify its existence and attempt to find, or refresh, metadata for it.
 *
 * <p>This is done to enrich, and then maintain, the {@link Song} data stored in the system.
 *
 * @implNote The lookup queue is derived from the {@link SongLookup} table. As our expected
 *     data-size is somewhere in the region of 10,000 records, this approach seems suitable for the
 *     foreseeable future of the system.
 * @see Song#poll(RestClient)
 * @see Provider#lookup(Song, RestClient, Executor)
 */
@CoreService
public class Lookup {

  private static final Logger log = LoggerFactory.getLogger(Lookup.class);

  private final LookupConfig config;

  private final SongLookupRepository lookups;

  private final SongService songs;

  private final Executor executor;

  private final TransactionTemplate transaction;

  private final RestClient client;

  Lookup(
      LookupConfig config,
      SongService songs,
      SongLookupRepository lookups,
      Executor executor,
      PlatformTransactionManager transactionManager,
      RestClient client) {
    this.config = config;
    this.songs = songs;
    this.lookups = lookups;
    this.executor = executor;
    this.transaction = new TransactionTemplate(transactionManager);
    this.client = client;
  }

  /**
   * Retrieve the next eligible {@link Song}(s) from the lookup queue, if any.
   *
   * <p>This action claims (removes) them from the queue and marks them as {@link
   * Status#PROCESSING}. They should be submitted to {@link #lookup(SongLookup)}.
   *
   * @implNote We cannot not use the {@link Transactional} annotation on this method as it is {@code
   *     private}, and said annotation works through proxying. A method call from inside the object
   *     bypasses the proxy.
   * @see #lookup(SongLookup)
   */
  private List<SongLookup> next() {
    Instant threshold = Instant.now().minus(config.refreshThreshold());
    List<SongLookup> claimedCandidates =
        transaction.execute(
            (t) -> {
              List<SongLookup> candidates = lookups.findNext(threshold, config.maxJobSize());
              candidates.forEach(SongLookup::start);
              return candidates;
            });

    return claimedCandidates == null ? Collections.emptyList() : claimedCandidates;
  }

  /**
   * Perform {@link Lookup}(s) for {@link Song}(s).
   *
   * @apiNote This pops jobs of the lookup queue.
   * @implNote See implementation note on {@link #lookup(SongLookup)} for why this method is not
   *     marked with {@link Transactional}.
   * @see Song#poll(RestClient)
   * @see Provider#lookup(Song, RestClient, Executor)
   */
  public void lookup() {
    Collection<SongLookup> queue = next();

    if (queue.isEmpty()) {
      log.trace("Heimdall: ...");
      return;
    }

    log.debug("Heimdall: I witness {}...", DomainEntity.log(queue));
    queue.forEach(candidate -> executor.execute(() -> this.lookup(candidate)));
  }

  /**
   * Lookup a {@link Song} to verify its existence and attempt to find metadata for it.
   *
   * @implNote We do not use the {@link Transactional} annotation on this method as we do not want
   *     to hold a transaction open while we are making (potentially) several network calls.
   * @see Song#poll(RestClient)
   * @see Provider#lookup(Song, RestClient, Executor)
   */
  private void lookup(SongLookup lookup) {
    Song song = lookup.getSong();

    try {
      HttpStatusCode statusCode = song.poll(client);
      if (statusCode.is2xxSuccessful()) {
        searchForMetadata(lookup);
      } else if (statusCode.is3xxRedirection()) {
        log.debug(
            "{} redirects. URL updated. Lookup of redirection location deferred for later.",
            song.log());
        lookup.finish(Status.PENDING);
      } else if (statusCode.is4xxClientError()) {
        log.warn(
            """
            Host server reports a client error during routine poll of {}.
            The system cannot recover from this. Removed from Lookup Queue.\
            """,
            song.log());
        lookup.finish(Status.INVALID);
      } else if (statusCode.is5xxServerError()) {
        log.info(
            """
            Host server encountered an error during routine poll of {}.
            Will retry later.\
            """,
            song.log());
        lookup.failed();
      } else {
        log.warn(
            """
            Unexpected status code ({}) during routine poll of {}.
            Ignoring. Will retry later.\
            """,
            statusCode,
            song.log());
        lookup.failed();
      }
    } catch (Exception ex) {
      log.error(
          "Exception encountered while processing {} with url {}. Will retry later.",
          song.log(),
          song.getUrl(),
          ex);
      lookup.failed();
    }

    transaction.execute(
        (t) -> {
          lookups.save(lookup);
          songs.updateSong(song);
          return null;
        });
  }

  private void searchForMetadata(SongLookup lookup) {
    Song song = lookup.getSong();
    boolean hit = Provider.lookup(song, client, executor);
    if (hit) {
      lookup.finish(Status.DONE);
    } else {
      applyYouTubeResolutions(song);
      lookup.failed();
    }
  }

  /**
   * Attempt to resolve potential issues with YouTube URLs.
   *
   * @implNote YouTube share links add additional share identifier query parameters and URLs of
   *     videos inside a mix or playlist will contain query parameters to link to the list. Their
   *     oEmbed endpoint does not resolve these URLs as belonging to YouTube.
   *     <p>TODO Find a way to check if the video is already normalised, i.e. resolution was already
   *     applied and skip it.
   */
  private void applyYouTubeResolutions(Song song) {
    if (!Provider.YOUTUBE.likelyProvides(song)) {
      return;
    }

    URL url = song.toUrl();
    String query = url.getQuery();
    if (query == null) {
      return;
    }

    Optional<String> videoId =
        Arrays.stream(query.split("&"))
            .map(param -> param.split("="))
            .filter(keyValue -> keyValue.length == 2 && keyValue[0].equals("v"))
            .map(keyValue -> keyValue[1])
            .findFirst();

    if (videoId.isEmpty()) {
      return;
    }

    String normalisedUrl = "https://www.youtube.com/watch?v=" + videoId.get();
    if (normalisedUrl.equals(song.getUrl())) {
      log.warn("YouTube resolution for {} failed. Its URL is already normalised.", song.log());
      return;
    }

    log.debug(
        "{} is likely provided by YouTube, but there is an issue resolving metadata. "
            + "Normalising the URL through video id extraction.",
        song.log());
    song.setUrl(normalisedUrl);
  }

  /**
   * Attempt to recover hanging {@link Lookup} jobs.
   *
   * @apiNote While the job itself can recover from failure, if the worker running the job or the
   *     system fail the job will be left in {@link Status#PROCESSING}
   */
  @Transactional
  public void recover() {
    Instant jobTimeoutThreshold = Instant.now().minus(config.jobTimeoutThreshold());
    Collection<SongLookup> timedOutLookups =
        this.lookups.findAllByStatusAndLastModifiedBefore(Status.PROCESSING, jobTimeoutThreshold);

    if (timedOutLookups.isEmpty()) {
      log.trace("Charon: All is as it should be.");
      return;
    }

    timedOutLookups.forEach(SongLookup::failed);
    log.info("Charon: I have ferried {} from beyond.", DomainEntity.log(timedOutLookups));
  }
}
