package com.bobo.storage.core.playlist;

import com.bobo.storage.core.semantic.EntityRepository;
import java.util.Collection;
import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @implSpec {@link EntityRepository}
 */
@Repository
interface PlaylistRepository
    extends EntityRepository<Playlist, Integer>, CrudRepository<Playlist, Integer> {

  @NonNull <S extends Playlist> Collection<S> saveAll(@NonNull Iterable<S> entities);

  @NonNull Collection<Playlist> findAll();

  /**
   * @param nameFragment fragment of a {@link Playlist} {@code name} to search for. Neither {@code
   *     null}, nor blank ({@link String#isBlank()}).
   * @return {@code Collection} containing zero or more matching {@code Playlists}.
   */
  Collection<Playlist> findAllByNameContainingIgnoringCase(String nameFragment);
}
