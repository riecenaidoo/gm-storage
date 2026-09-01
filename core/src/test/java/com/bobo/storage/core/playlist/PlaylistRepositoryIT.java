package com.bobo.storage.core.playlist;

import com.bobo.semantic.IntegrationTest;
import com.bobo.semantic.TestInfrastructure;
import com.bobo.storage.core.semantic.RepositoryTest;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.repository.CrudRepository;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;

/**
 * Non-exhaustive persistence layer integration tests.
 *
 * <p>Given a valid entity and repository, can Data JPA still persist it and derive these repository
 * methods correctly?
 */
@IntegrationTest({PlaylistRepository.class, CrudRepository.class})
@RepositoryTest
class PlaylistRepositoryIT {

  @Container @ServiceConnection
  private static final JdbcDatabaseContainer<?> database = TestInfrastructure.getDatabase();

  // Test Utilities

  private final Random random = new Random();

  // Test Targets

  private final PlaylistRepository repository;

  @Autowired
  PlaylistRepositoryIT(PlaylistRepository repository) {
    this.repository = repository;
  }

  /**
   * @see PlaylistRepository#findAllByNameContainingIgnoringCase(String)
   */
  @Test
  void findAllByNameContainingIgnoringCase() {
    // Given
    PlaylistMother mother = new PlaylistMother(random);
    Collection<Playlist> playlists =
        Stream.of(
                "Rhythm & Relaxation",
                "Softness & Relaxation",
                "Summer Warmth",
                "Chill Vibes",
                "Salsa",
                "Nostalgia - 2022",
                "Nostalgia - 2024")
            .map(name -> mother.withNames(() -> name).get())
            .toList();
    repository.saveAll(playlists);

    // When
    String nameFragmentQuery = "relax";
    playlists = repository.findAllByNameContainingIgnoringCase(nameFragmentQuery);

    // Then
    Assertions.assertEquals(2, playlists.size());
    for (Playlist playlist : playlists) {
      Assertions.assertTrue(
          playlist.getName().toLowerCase().contains(nameFragmentQuery),
          "Query returned result that does not contain name fragment.");
    }
  }
}
