package com.bobo.storage.core.song;

import com.bobo.semantic.IntegrationTest;
import com.bobo.semantic.TechnicalID;
import com.bobo.semantic.TestInfrastructure;
import com.bobo.storage.core.semantic.RepositoryTest;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;
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
@IntegrationTest({SongRepository.class, CrudRepository.class})
@RepositoryTest
class SongRepositoryIT {

  @Container @ServiceConnection
  private static final JdbcDatabaseContainer<?> database = TestInfrastructure.getDatabase();

  // Test Utilities

  private final Random random = new Random();

  private final SongLookupTestRepository songLookupRepository;

  // Test Targets

  private final SongRepository songRepository;

  @Autowired
  SongRepositoryIT(SongLookupTestRepository songLookupRepository, SongRepository repository) {
    this.songLookupRepository = songLookupRepository;
    this.songRepository = repository;
  }

  /**
   * @see SongRepository#findByUrl(String)
   */
  @Test
  void findByUrl() {
    // Given
    Collection<Song> songs = new SongMother(random).withUrls().get(5).toList();
    songRepository.saveAll(songs);
    Song song = songs.iterator().next();

    // When
    Optional<Song> retrievedSong = songRepository.findByUrl(song.getUrl());

    // Then
    Assertions.assertTrue(retrievedSong.isPresent());
    Assertions.assertEquals(song.getUrl(), retrievedSong.get().getUrl());
  }

  /**
   * @see SongRepository#findJoblessSongs()
   */
  @Test
  void findJoblessSongs() {
    // Given
    SongMother songMother = new SongMother(random);
    Collection<Song> songs = songMother.get(5).toList();
    songs = songRepository.saveAll(songs);

    SongLookupMother lookupMother = new SongLookupMother(random);
    Collection<SongLookup> lookupJobs =
        songs.stream().map(song -> lookupMother.withSongs(() -> song).get()).toList();
    songLookupRepository.saveAll(lookupJobs);

    Song songWithoutLookup = songMother.get();
    songWithoutLookup = songRepository.save(songWithoutLookup);

    // When
    Collection<Song> joblessSongs = songRepository.findJoblessSongs();

    // Then
    Assertions.assertEquals(1, joblessSongs.size());
    Assertions.assertTrue(TechnicalID.same(songWithoutLookup, joblessSongs.iterator().next()));
  }
}
