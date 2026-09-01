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

  /**
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  @NonNull <S extends Playlist> Collection<S> saveAll(@NonNull Iterable<S> entities);

  /**
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  @NonNull Collection<Playlist> findAll();

  /**
   * @param nameFragment a part of a {@link Playlist#getName() name} to search for. Neither {@code
   *     null}, nor ({@link String#isBlank() blank}).
   * @return {@link Playlist}(s) matching the criteria.
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  Collection<Playlist> findAllByNameContainingIgnoringCase(String nameFragment);
}
