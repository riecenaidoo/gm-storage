package com.bobo.storage.core.playlist.song;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.semantic.AccessForTesting;
import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.song.Song;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

/**
 * A {@link Song} within a {@link Playlist}.
 *
 * <p>Represents the relationship of the {@code Song} within the {@code Playlist}, which in future
 * may contain information such as 'order', 'favourited', 'preferredVendor', etc.
 */
@Entity
public class PlaylistSong extends DomainEntity {

  @ManyToOne(optional = false)
  private Playlist playlist;

  @ManyToOne(optional = false)
  private Song song;

  /**
   * @see DomainEntity#DomainEntity()
   */
  protected PlaylistSong() {}

  /**
   * Create a relationship between a {@link Playlist} and a {@link Song}.
   *
   * <p>The {@link Playlist} is the owning side of the relationship, it must already exist. A {@link
   * PlaylistSong} can serve as a creation root for a {@link Song}; if the {@link Song} does not
   * exist, it will be created.
   *
   * @param playlist the {@link Playlist} to add the {@link Song} into. Must have an assigned {@link
   *     TechnicalID}.
   * @param song the {@link Song} to add to the {@link Playlist}.
   */
  public PlaylistSong(Playlist playlist, Song song) {
    // Existence Validation
    Objects.requireNonNull(playlist, "Playlist argument must not be null.");
    Objects.requireNonNull(song, "Song argument must not be null.");

    // State Validation
    if (playlist.getId() == null) {
      throw new IllegalArgumentException("Playlist argument must have an assigned TechnicalID.");
    }

    this.playlist = playlist;
    this.song = song;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PlaylistSong that)) return false;
    return Objects.equals(playlist, that.playlist) && Objects.equals(song, that.song);
  }

  @Override
  public int hashCode() {
    return Objects.hash(playlist, song);
  }

  public Playlist getPlaylist() {
    return playlist;
  }

  @AccessForTesting(AccessForTesting.Modifier.PACKAGE_PRIVATE)
  void setPlaylist(Playlist playlist) {
    this.playlist = playlist;
  }

  public Song getSong() {
    return song;
  }

  @AccessForTesting(AccessForTesting.Modifier.PACKAGE_PRIVATE)
  void setSong(Song song) {
    this.song = song;
  }

  void migrate(Song to) {
    // TODO Validate their URLs are the same.
    setSong(to);
  }
}
