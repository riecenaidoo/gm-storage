package com.bobo.storage.core.playlist.song;

import com.bobo.semantic.UnitTest;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistMother;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMother;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@UnitTest(PlaylistSong.class)
class PlaylistSongTest {

  private final Random random = new Random();

  /**
   * TODO Should the exception messages be stored somewhere to verify the right message is used in
   * these tests? Right now we are just confirming the right exception is thrown, but the reason
   * should be confirmed as well no?
   *
   * @see PlaylistSong#PlaylistSong(Playlist, Song)
   */
  @Nested
  class Construction {

    /** Control test to verify the happy path. */
    @Test
    @DisplayName("A PlaylistSong can be constructed from valid arguments")
    void canBeConstructed() {
      Playlist playlist = new PlaylistMother(random).withAll().get();
      Song song = new SongMother(random).withIds().get();

      PlaylistSong playlistSong =
          Assertions.assertDoesNotThrow(() -> new PlaylistSong(playlist, song));
      Assertions.assertSame(playlist, playlistSong.getPlaylist());
      Assertions.assertSame(song, playlistSong.getSong());
    }

    @Test
    @DisplayName("Playlist argument must have an assigned TechnicalId")
    void unassignedPlaylist() {
      Playlist playlist = new PlaylistMother(random).withIds(() -> null).get();
      Song song = new SongMother(random).withIds().get();

      Assertions.assertThrows(
          IllegalArgumentException.class, () -> new PlaylistSong(playlist, song));
    }

    /**
     * The contract language, {@code "A PlaylistSong can only be created by adding a Song into a
     * Playlist..."}, implies the existence of both the {@link Playlist} and {@link Song}.
     */
    @Test
    @DisplayName("Playlist and Song must exist")
    void missingArguments() {
      Playlist playlist = new PlaylistMother(random).withAll().get();
      Song song = new SongMother(random).withIds().get();

      Assertions.assertThrows(
          NullPointerException.class,
          () -> new PlaylistSong(null, song),
          "There must be a Playlist, to add the Song into");

      Assertions.assertThrows(
          NullPointerException.class,
          () -> new PlaylistSong(playlist, null),
          "There must be a Song, to add into the Playlist");
    }
  }
}
