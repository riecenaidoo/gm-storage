package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.EntityRepository;
import com.bobo.storage.core.semantic.Read;
import java.util.Collection;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @implSpec {@link EntityRepository}
 */
@Repository
interface SongRepository extends EntityRepository<Song, Integer>, CrudRepository<Song, Integer> {

  /**
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  @NonNull <S extends Song> Collection<S> saveAll(@NonNull Iterable<S> entities);

  /**
   * @param url {@link Song#getUrl()}
   * @return the {@link Song} if found, otherwise {@link Optional#empty()}.
   * @implSpec {@link Read#find(int)}
   */
  Optional<Song> findByUrl(String url);

  /**
   * @return {@link Song}(s) that do not have corresponding {@link SongLookup} job entries.
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  @Query(
      value =
"""
SELECT * FROM song
LEFT JOIN song_lookup ON song_lookup.song_id = song.id
WHERE song_lookup.song_id IS NULL
""",
      nativeQuery = true)
  Collection<Song> findJoblessSongs();
}
