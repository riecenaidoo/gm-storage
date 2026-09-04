package com.bobo.storage.core.tag.song;

import com.bobo.storage.core.semantic.EntityRepository;
import com.bobo.storage.core.song.Song;
import java.util.Collection;
import org.springframework.data.repository.CrudRepository;

/**
 * @implSpec {@link EntityRepository}
 */
interface SongTagRepository
    extends EntityRepository<SongTag, Integer>, CrudRepository<SongTag, Integer> {

  /**
   * @return {@link SongTag}(s) linked to the {@link Song}.
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  Collection<SongTag> findAllBySong(Song from);
}
