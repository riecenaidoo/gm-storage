package com.bobo.storage.core.song;

import com.bobo.semantic.IntegrationTest;
import com.bobo.semantic.TechnicalID;
import com.bobo.semantic.TestInfrastructure;
import com.bobo.storage.core.semantic.RepositoryTest;
import com.bobo.storage.core.song.SongLookup.JobStatus;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.repository.CrudRepository;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;

@IntegrationTest({SongLookupRepository.class, CrudRepository.class})
@RepositoryTest
class SongLookupRepositoryIT {

  @Container @ServiceConnection
  private static final JdbcDatabaseContainer<?> database = TestInfrastructure.getDatabase();

  // Test Utilities

  private final Random random = new Random();

  private final SongRepository songs;

  // Test Targets

  private final SongLookupRepository repository;

  @Autowired
  SongLookupRepositoryIT(SongLookupRepository repository, SongRepository songs) {
    this.repository = repository;
    this.songs = songs;
  }

  /**
   * @see SongLookupRepository#findBySong(Song)
   */
  @Test
  void findBySong() {
    // Given
    Song song = new SongMother(random).get();
    song = songs.save(song);
    repository.save(new SongLookup(song));

    // When
    Optional<SongLookup> lookup = repository.findBySong(song);

    // Then
    Assertions.assertTrue(lookup.isPresent());
    Assertions.assertSame(song, lookup.get().getSong());
  }

  /**
   * @see SongLookupRepository#findNext(Instant, int)
   */
  @Nested
  class FindNext {

    @Test
    void whereFilter() {
      // Given
      final Instant threshold = Instant.now();

      SongMother songMother = new SongMother(random);
      SongLookupMother mother =
          new SongLookupMother(random)
              .withSongs(() -> songs.save(songMother.get()))
              .withLastLookup(() -> threshold);

      List<SongLookup> lookups =
          Arrays.stream(JobStatus.values())
              .map((status) -> mother.withStatuses(() -> status).get())
              .toList();

      repository.saveAll(lookups);

      // When
      lookups = repository.findNext(threshold.minusSeconds(60), 5);

      // Then
      long done = count(lookups, JobStatus.DONE);
      Assertions.assertEquals(0, done, "Work DONE after threshold should not be returned.");
      assertThresholdFilterInvariants(lookups);

      // When
      lookups = repository.findNext(threshold.plusSeconds(60), 5);

      // Then
      done = count(lookups, JobStatus.DONE);
      Assertions.assertEquals(1, done, "Work DONE before threshold should be returned.");
      assertThresholdFilterInvariants(lookups);
    }

    private void assertThresholdFilterInvariants(Collection<SongLookup> lookups) {
      long pending = count(lookups, JobStatus.PENDING);
      long processing = count(lookups, JobStatus.PROCESSING);
      long invalid = count(lookups, JobStatus.INVALID);

      Assertions.assertEquals(1, pending, "PENDING work should always be returned.");
      Assertions.assertEquals(
          0, processing, "Work undergoing PROCESSING should never be returned.");
      Assertions.assertEquals(0, invalid, "INVALID work should never be returned.");
    }

    private long count(Collection<SongLookup> lookups, JobStatus status) {
      return lookups.stream().map(SongLookup::getStatus).filter(status::equals).count();
    }

    /**
     *
     *
     * <ul>
     *   <li>Lookups are ordered by {@link SongLookup#setLastLookup(Instant) lastLookup}, oldest
     *       first.
     *   <li>A null {@code lastLookup} means the song has never been looked up and is therefore
     *       treated as older than any non-null value.
     *   <li>{@link JobStatus#PENDING PENDING} lookups are ordered before {@link JobStatus#DONE}
     *       lookups.
     * </ul>
     */
    @Test
    void sortOrder() {
      // Given
      final Instant threshold = Instant.now();

      SongMother songMother = new SongMother(random);
      SongLookupMother mother =
          new SongLookupMother(random).withSongs(() -> songs.save(songMother.get()));

      SongLookup pendingNeverLookedUp =
          repository.save(
              mother.withStatuses(() -> JobStatus.PENDING).withLastLookup(() -> null).get());
      SongLookup pendingButLookedUpBefore =
          repository.save(
              mother
                  .withStatuses(() -> JobStatus.PENDING)
                  .withLastLookup(() -> threshold.minusSeconds(60))
                  .get());
      SongLookup oldestProcessed =
          repository.save(
              mother
                  .withStatuses(() -> JobStatus.DONE)
                  .withLastLookup(() -> threshold.minusSeconds(600))
                  .get());
      SongLookup mostRecentlyProcessed =
          repository.save(
              mother
                  .withStatuses(() -> JobStatus.DONE)
                  .withLastLookup(() -> threshold.minusSeconds(60))
                  .get());

      // When
      List<SongLookup> lookups = repository.findNext(threshold, 5);

      // Then
      Assertions.assertEquals(4, lookups.size());
      Assertions.assertTrue(TechnicalID.same(pendingNeverLookedUp, lookups.getFirst()));
      Assertions.assertTrue(TechnicalID.same(pendingButLookedUpBefore, lookups.get(1)));
      Assertions.assertTrue(TechnicalID.same(oldestProcessed, lookups.get(2)));
      Assertions.assertTrue(TechnicalID.same(mostRecentlyProcessed, lookups.getLast()));
    }

    /** Written query, not generated, so ensure limit is in place. */
    @Test
    void limits() {
      // Given
      final Instant threshold = Instant.now();
      SongMother songMother = new SongMother(random);
      SongLookupMother mother =
          new SongLookupMother(random)
              .withSongs(() -> songs.save(songMother.get()))
              .withLastLookup(() -> threshold);

      repository.saveAll(mother.get(6).toList());

      // When
      List<SongLookup> lookups = repository.findNext(threshold.minusSeconds(60), 5);

      // Then
      Assertions.assertEquals(5, lookups.size());
    }
  }

  /**
   * @see SongLookupRepository#findAllByStatusAndLastModifiedBefore(JobStatus, Instant)
   */
  @Test
  void findAllByStatusAndLastModifiedBefore() {
    // Given
    final Instant threshold = Instant.now();
    JobStatus desiredStatus = JobStatus.PROCESSING;

    SongMother songMother = new SongMother(random);
    SongLookupMother mother =
        new SongLookupMother(random)
            .withSongs(() -> songs.save(songMother.get()))
            .withStatuses(() -> desiredStatus);

    repository.save(mother.withLastModified(() -> threshold.plusSeconds(60)).get());
    SongLookup expectedMatch =
        repository.save(mother.withLastModified(() -> threshold.minusSeconds(60)).get());
    repository.save(
        mother
            .withStatuses(() -> JobStatus.PENDING)
            .withLastModified(() -> threshold.minusSeconds(60))
            .get());

    // When
    Collection<SongLookup> lookups =
        repository.findAllByStatusAndLastModifiedBefore(desiredStatus, threshold);

    // Then
    Assertions.assertEquals(1, lookups.size());
    Assertions.assertTrue(TechnicalID.same(expectedMatch, lookups.iterator().next()));
  }
}
