package com.bobo.storage.core.semantic;

import com.bobo.semantic.UnitTest;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistMother;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMother;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@UnitTest(DomainEntity.class)
class DomainEntityTest {

  /**
   * To be used for creating arbitrary values that will <b>never</b> change during the scope of the
   * test they were created within.
   *
   * <p>Mark such values {@code final} where possible to declare the intention.
   */
  private final Random random = new Random();

  @Test
  @DisplayName("Produces a log in the format Entity(id:...)")
  void log() {
    // Given
    DomainEntity entity = new SongMother(random).withIds(() -> 1).get();
    // When
    String log = entity.log();
    // Then
    Assertions.assertEquals("Song(id:1)", log);
  }

  /**
   * @see DomainEntity#log(Collection)
   */
  @Nested
  class LogCollection {

    @Test
    @DisplayName("Produces a log in the format n Entity(ids:...,...)")
    void log() {
      // Given
      List<Song> entities = new SongMother(random).get(3).toList();
      EntityMother.setId(entities.get(0), 1);
      EntityMother.setId(entities.get(1), 2);
      EntityMother.setId(entities.get(2), 3);
      // When
      String log = DomainEntity.log(entities);
      // Then
      Assertions.assertEquals("3 Song(ids:1, 2, 3)", log);
    }

    @Test
    @DisplayName("Throws IllegalArgumentException if the collection is empty.")
    void empty() {
      // Given
      Collection<DomainEntity> entities = Collections.emptyList();
      // When
      Assertions.assertThrows(
          IllegalArgumentException.class,
          // Then
          () -> DomainEntity.log(entities));
    }

    @Test
    @DisplayName("Throws IllegalArgumentException if the collection is non-homogenous.")
    void nonHomogeneous() {
      // Given
      Playlist playlist = new PlaylistMother(random).withIds().get();
      Song song = new SongMother(random).withIds().get();
      Collection<DomainEntity> entities = List.of(playlist, song);
      // When
      Assertions.assertThrows(
          IllegalArgumentException.class,
          // Then
          () -> DomainEntity.log(entities));
    }
  }
}
