package com.bobo.storage.scheduling;

import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.song.LookupService;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** The schedule of jobs the system should run, and when. */
@Component
public class JobSchedule {

  private static final Logger log = LoggerFactory.getLogger(JobSchedule.class);

  private final SongService songs;

  private final LookupService lookupService;

  public JobSchedule(SongService songs, LookupService lookupService) {
    this.songs = songs;
    this.lookupService = lookupService;
  }

  /**
   * Performs the initial lookup for a {@link Song}.
   *
   * <p>Since metadata may not be available for every URL and isn't required in all use cases, this
   * process is separated from the actual creation of the {@link Song}. This task runs as frequently
   * as possible to provide near real-time feedback to the user without blocking their workflow.
   *
   * <p>Although the lookup of each individual {@link Song} is atomic, the job as a whole is not
   * designed to run concurrently. Therefore, only one instance of this job should execute at any
   * given time. The current implementation relies on Spring's default single-threaded scheduler to
   * enforce this behavior.
   *
   * <p>For concurrency, we would need to look at adding some kind of "lock" on a {@link Song} while
   * it is being looked up to prevent multiple jobs looking it up and causing issues.
   */
  @Scheduled(cron = "0 * * * * *")
  public void lookupNewSongs() {
    Collection<Song> songsToLookup = songs.getLookupCandidates();
    if (songsToLookup.isEmpty()) {
      return;
    }
    if (log.isTraceEnabled()) {
      String urlQueue =
          songsToLookup.stream().map(Song::getUrl).collect(Collectors.joining("\n\t - "));
      log.trace(
          "Job#LookupNewSongs: Looking up {}...\n\t - {}",
          DomainEntity.log(songsToLookup),
          urlQueue);
    } else if (log.isInfoEnabled()) {
      log.info("Job#LookupNewSongs: Looking up {}...", DomainEntity.log(songsToLookup));
    }

    songsToLookup.forEach(lookupService::lookup);
  }
}
