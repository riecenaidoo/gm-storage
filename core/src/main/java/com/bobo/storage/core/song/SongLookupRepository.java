package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.EntityRepository;
import com.bobo.storage.core.song.SongLookup.JobStatus;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @implSpec {@link EntityRepository}
 */
@Repository
interface SongLookupRepository
    extends EntityRepository<SongLookup, Integer>, CrudRepository<SongLookup, Integer> {

  Optional<SongLookup> findBySong(Song song);

  /**
   * @implNote A native query is used because the use of {@link Sort.Order#nullsFirst()} will cause
   *     the exception, {@code Applying Null Precedence using Criteria Queries is not yet supported}
   *     in the current version of SpringBoot ({@code 3.4.2}). The default null precedence for
   *     ascending order sorts in PostgresSQL is nulls last, so we need to specify this explicitly.
   */
  @Query(
      value =
"""
SELECT *
FROM song_lookup s
WHERE
    s.status = 'PENDING'
    OR (
        s.status = 'DONE'
        AND s.last_lookup < :threshold
    )
ORDER BY
    (s.status = 'PENDING') DESC,
    s.last_lookup ASC NULLS FIRST
LIMIT :limit
FOR UPDATE SKIP LOCKED
""",
      nativeQuery = true)
  List<SongLookup> findNext(@Param("threshold") Instant threshold, @Param("limit") int limit);

  Collection<SongLookup> findAllByStatusAndLastModifiedBefore(JobStatus status, Instant instant);
}
