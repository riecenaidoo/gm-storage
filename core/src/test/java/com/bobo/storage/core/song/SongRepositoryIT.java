package com.bobo.storage.core.song;

import com.bobo.semantic.IntegrationTest;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.repository.CrudRepository;

/**
 * These tests are specifically for repository methods I define using the query language. I do not
 * need to test the supplied methods like {@code findById}, etc. I just need to confirm the query
 * methods I define work as I expect.
 *
 * <p>They probably would only be necessary to re-run if I change the domain model, otherwise they
 * can be skipped. TODO Figure out if I could define a test suite?
 */
@IntegrationTest({SongRepository.class, CrudRepository.class})
@DataJpaTest
class SongRepositoryIT {

  // Test Utilities

  private final Random random = new Random();

  // Test Targets

  private final SongRepository repository;

  @Autowired
  SongRepositoryIT(SongRepository repository) {
    this.repository = repository;
  }

  @Test
  void findByUrl() {
    // Given
    Song song = new SongMother(random).withUrls().get();
    song = repository.save(song);
    Assertions.assertTrue(repository.findById(song.getId()).isPresent(), "Test assumption failed.");

    // When
    Song retrievedSong = repository.findByUrl(song.getUrl()).orElseThrow(AssertionError::new);

    // Then
    Assertions.assertEquals(song, retrievedSong);
  }
}
