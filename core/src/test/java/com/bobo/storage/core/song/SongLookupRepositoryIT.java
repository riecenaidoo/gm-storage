package com.bobo.storage.core.song;

import com.bobo.semantic.IntegrationTest;
import java.time.Instant;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@IntegrationTest({SongLookupRepository.class, CrudRepository.class})
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SongLookupRepositoryIT {

  @Container @ServiceConnection
  static PostgreSQLContainer database = new PostgreSQLContainer("postgres:14.12-bullseye");

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
