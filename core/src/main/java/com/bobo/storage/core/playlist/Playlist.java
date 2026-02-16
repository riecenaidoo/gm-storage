package com.bobo.storage.core.playlist;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.semantic.DomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.Objects;

@Entity
public class Playlist extends DomainEntity {

  @Column(nullable = false)
  private String name;

  /**
   * @see DomainEntity#DomainEntity()
   */
  protected Playlist() {}

  public Playlist(String name) {
    this.name = name;
  }

  /**
   * Copy constructor.
   *
   * <p>The {@link TechnicalID} is never copied.
   *
   * @param playlist to copy.
   */
  public Playlist(Playlist playlist) {
    this(playlist.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Playlist playlist)) return false;
    return Objects.equals(name, playlist.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
