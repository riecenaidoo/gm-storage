package com.bobo.storage.core.song;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bobo.semantic.UnitTest;
import com.bobo.storage.core.semantic.EntityMother;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@UnitTest(SongService.class)
@ExtendWith(MockitoExtension.class)
class SongServiceTest {

  @Mock private SongRepository repository;

  @Mock private SongLookupRepository lookupRepository;

  // Test Utilities

  private final Random random = new Random();

  // Test Targets

  @InjectMocks private SongService service;

  /**
   * @see SongService#add(Song)
   */
  @Nested
  class Create {

    @Test
    void addSong() {
      // Given
      Song song = new SongMother(random).withUrls().get();

      // Stubbing
      when(repository.findByUrl(song.getUrl())).thenReturn(Optional.empty());
      when(repository.save(song)).then((ans) -> EntityMother.setId(song, 1));

      // When
      Song createdSong = service.add(song);

      // Then
      Assertions.assertEquals(song, createdSong);
      verify(repository, times(1)).save(song);
    }

    @DisplayName("Throw IllegalArgumentException if song has a TechnicalID assigned.")
    @Test
    void songHasATechnicalID() {
      // Given
      Song song = new SongMother(random).withIds().withUrls().get();

      // Then
      Assertions.assertThrows(
          IllegalArgumentException.class,
          // When
          () -> service.add(song));
    }

    @DisplayName("Do not save another Song, if an entry for it already exists.")
    @Test
    void songAlreadyExists() {
      SongMother mother = new SongMother(random).withUrls();
      // Given
      Song song = mother.get();
      Song existingEntity = mother.withUrls(song::getUrl).withIds().get();

      // Stubbing
      when(repository.findByUrl(song.getUrl())).thenReturn(Optional.of(existingEntity));

      // When
      Song createdSong = service.add(song);

      // Then
      Assertions.assertEquals(
          song, createdSong, "Returned Song must be equivalent to Song argument.");
      Assertions.assertSame(
          existingEntity, createdSong, "Should return the managed entity for further use.");
      verify(repository, times(0)).save(song);
    }
  }
}
