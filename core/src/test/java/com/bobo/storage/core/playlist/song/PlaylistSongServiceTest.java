package com.bobo.storage.core.playlist.song;

import com.bobo.semantic.UnitTest;
import com.bobo.storage.core.song.SongService;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest(PlaylistSongService.class)
@ExtendWith(MockitoExtension.class)
class PlaylistSongServiceTest {

  @Mock private PlaylistSongRepository playlistSongs;

  @Mock private SongService songs;

  // Test Utilities

  private final Random random = new Random();

  // Test Targets

  @InjectMocks private PlaylistSongService service;

  /**
   * @see PlaylistSongService#add(PlaylistSong)
   */
  @Nested
  class Create {

    @DisplayName("Throw IllegalArgumentException if song has a TechnicalID assigned.")
    @Test
    void songHasATechnicalID() {
      // Given
      PlaylistSong song = new PlaylistSongMother(random).withIds().get();

      // Then
      Assertions.assertThrows(
          IllegalArgumentException.class,
          // When
          () -> service.add(song));
    }
  }
}
