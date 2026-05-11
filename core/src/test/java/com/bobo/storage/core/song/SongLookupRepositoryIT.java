package com.bobo.storage.core.song;

import com.bobo.semantic.IntegrationTest;
import com.bobo.semantic.TestInfrastructure;
import com.bobo.storage.core.semantic.RepositoryTest;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
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
  @Test
  void findNext() {}

  /**
   * @see SongLookupRepository#findAllByStatusAndLastModifiedBefore(SongLookup.JobStatus, Instant)
   */
  @Test
  void findAllByStatusAndLastModifiedBefore() {}
}
