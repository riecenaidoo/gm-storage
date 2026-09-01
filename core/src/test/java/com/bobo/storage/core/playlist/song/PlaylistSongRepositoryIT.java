package com.bobo.storage.core.playlist.song;

import com.bobo.semantic.IntegrationTest;
import com.bobo.semantic.TechnicalID;
import com.bobo.semantic.TestInfrastructure;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistMother;
import com.bobo.storage.core.playlist.PlaylistTestRepository;
import com.bobo.storage.core.semantic.RepositoryTest;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMother;
import com.bobo.storage.core.song.SongTestRepository;
import java.util.Collection;
import java.util.List;
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
 * <p>Given a valid {@link PlaylistSong} entity and repository, can Spring Data JPA still persist it
 * and derive these repository methods correctly?
 */
@IntegrationTest({PlaylistSongRepository.class, CrudRepository.class})
@RepositoryTest
class PlaylistSongRepositoryIT {

  @Container @ServiceConnection
  private static final JdbcDatabaseContainer<?> database = TestInfrastructure.getDatabase();

  // Test Utilities

  private final Random random = new Random();

  private final PlaylistTestRepository playlistRepository;

  private final SongTestRepository songRepository;

  // Test Targets

  private final PlaylistSongRepository repository;

  @Autowired
  PlaylistSongRepositoryIT(
      PlaylistSongRepository repository,
      PlaylistTestRepository playlistRepository,
      SongTestRepository songRepository) {
    this.repository = repository;
    this.playlistRepository = playlistRepository;
    this.songRepository = songRepository;
  }

  /**
   * @see PlaylistSongRepository#findAllByPlaylist(Playlist)
   */
  @Test
  void findAllByPlaylist() {
    // Given
    List<Playlist> playlists = new PlaylistMother(random).get(5).toList();
    playlistRepository.saveAll(playlists);
    Playlist playlist = playlists.getFirst();

    Song song = songRepository.save(new SongMother(random).get());

    repository.saveAll(playlists.stream().map(p -> new PlaylistSong(p, song)).toList());

    // When
    Collection<PlaylistSong> playlistSongs = repository.findAllByPlaylist(playlist);

    // Then
    Assertions.assertEquals(1, playlistSongs.size());
    PlaylistSong playlistSong = playlistSongs.iterator().next();
    Assertions.assertTrue(TechnicalID.same(playlist, playlistSong.getPlaylist()));
  }

  /**
   * @see PlaylistSongRepository#findAllBySong(Song)
   */
  @Test
  void findAllBySong() {
    // Given
    Playlist playlist = playlistRepository.save(new PlaylistMother(random).get());

    List<Song> songs = new SongMother(random).get(5).toList();
    songRepository.saveAll(songs);
    Song song = songs.getFirst();

    repository.saveAll(songs.stream().map(s -> new PlaylistSong(playlist, s)).toList());

    // When
    Collection<PlaylistSong> playlistSongs = repository.findAllBySong(song);

    // Then
    Assertions.assertEquals(1, playlistSongs.size());
    PlaylistSong playlistSong = playlistSongs.iterator().next();
    Assertions.assertTrue(TechnicalID.same(song, playlistSong.getSong()));
  }
}
