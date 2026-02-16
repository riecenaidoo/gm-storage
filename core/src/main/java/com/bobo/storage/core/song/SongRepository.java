package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.EntityRepository;
import com.bobo.storage.core.semantic.Read;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @implSpec {@link EntityRepository}
 */
@Repository
interface SongRepository extends EntityRepository<Song, Integer>, CrudRepository<Song, Integer> {

  /**
   * Find a {@link Song} by its {@code url}, which uniquely identifies it.
   *
   * @param url the unique reference {@code url} of the {@link Song}.
   * @return the {@link Song} if found, otherwise Optional.empty().
   * @implSpec {@link Read#find(int)}
   */
  Optional<Song> findByUrl(String url);

  /**
   * @return set of {@link Song} that do not have corresponding {@link SongLookup} entries.
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
