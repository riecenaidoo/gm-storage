package com.bobo.storage.core.tag.song;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.tag.Tag;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

/** A unique {@link Tag} of a {@link Song}. */
@Entity
@Table(
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_song_tag",
            columnNames = {"song_id", "tag_id"}))
public class SongTag extends DomainEntity {

  @ManyToOne(optional = false)
  private Song song;

  @ManyToOne(optional = false)
  private Tag tag;

  /**
   * @see DomainEntity#DomainEntity()
   */
  protected SongTag() {}

  /**
   * {@link Tag} a {@link Song}.
   *
   * <p>The {@link Song} is the owning side of the relationship, it must already exist. A {@link
   * SongTag} can serve as a creation root for a {@link Tag}; if the {@link Tag} does not already
   * exist, it will be created.
   *
   * @param song the {@link Song} to tag. Must have an assigned {@link TechnicalID}.
   * @param tag the {@link Tag} to add.
   */
  public SongTag(Song song, Tag tag) {
    // Existence Validation
    Objects.requireNonNull(song, "Song argument must not be null.");
    Objects.requireNonNull(tag, "Tag argument must not be null.");

    // State Validation
    if (song.getId() == null) {
      throw new IllegalArgumentException("Song argument must have an assigned TechnicalID.");
    }
    this.song = song;
    this.tag = tag;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    SongTag songTag = (SongTag) o;
    return Objects.equals(song, songTag.song) && Objects.equals(tag, songTag.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(song, tag);
  }

  public Song getSong() {
    return song;
  }

  public Tag getTag() {
    return tag;
  }
}
