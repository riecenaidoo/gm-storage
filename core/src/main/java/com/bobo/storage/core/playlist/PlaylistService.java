package com.bobo.storage.core.playlist;

import com.bobo.storage.core.semantic.CoreService;
import com.bobo.storage.core.semantic.Create;
import com.bobo.storage.core.semantic.EntityService;
import com.bobo.storage.core.semantic.Read;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@CoreService
public class PlaylistService implements EntityService<Playlist>, Create<Playlist>, Read<Playlist> {

  private final PlaylistRepository playlists;

  PlaylistService(PlaylistRepository playlistRepository) {
    this.playlists = playlistRepository;
  }

  @Override
  @Transactional
  public Playlist add(Playlist playlist) {
    if (Objects.nonNull(playlist.getId())) throw new IllegalArgumentException();

    return playlists.save(playlist);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Playlist> find(int id) {
    return playlists.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Collection<Playlist> get() {
    return playlists.findAll();
  }

  /**
   * @param name a case-insensitive {@code name}, or part thereof, to search for.
   * @return a {@link Collection} of {@link Playlist} resources that match, or contain, the queried
   *     {@code name}; never null.
   * @apiNote In future, this method will support flags to control case sensitivity, word matching,
   *     prefix matching, and other search behaviors.
   * @implSpec {@link Read#get()}
   * @implNote We should explore support for more advanced search functionality in the future,
   *     including semantic search via external libraries or embedded LLMs.
   *     <p>We should also consider introducing relevance scoring to rank results based on match
   *     quality.
   */
  @Transactional(readOnly = true)
  public Collection<Playlist> searchByName(String name) {
    return playlists.findAllByNameContainingIgnoringCase(name);
  }

  @Transactional
  public Playlist update(Playlist playlist) {
    return playlists.save(playlist);
  }

  /** TODO: Consider marking inactive rather than removing. */
  @Transactional
  public void delete(Playlist playlist) {
    playlists.delete(playlist);
  }
}
