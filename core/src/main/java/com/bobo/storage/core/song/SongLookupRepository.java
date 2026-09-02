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

  /**
   * @return the {@link SongLookup} if found, otherwise {@link Optional#empty()}.
   */
  Optional<SongLookup> findBySong(Song song);

  /**
   * Retrieve the next {@link SongLookup} jobs in the processing queue.
   *
   * <ul>
   *   <li>{@link JobStatus#PENDING Pending} work is always eligible for processing.
   *   <li>After a successful {@link Lookup} cycle, a {@link SongLookup} is marked {@link
   *       JobStatus#DONE complete} and removed from the queue.
   *   <li>{@link JobStatus#DONE Completed} work becomes eligible for processing once the {@link
   *       Song} metadata is considered stale. It is considered stale if its {@link
   *       SongLookup#setLastLookup(Instant) lastLookup} was older than some {@code threshold}.
   * </ul>
   *
   * @param threshold exclusive upper-bound for a {@link JobStatus#DONE completed} {@link SongLookup
   *     job's} {@link SongLookup#setLastLookup(Instant) lastLookup} timestamp.
   * @param limit the upper limit of jobs to retrieve from the processing queue.
   * @return an ordered collection of {@link SongLookup}(s).
   *     <ul>
   *       <li>Lookups are ordered by {@link SongLookup#setLastLookup(Instant) lastLookup}, oldest
   *           first.
   *       <li>A null {@code lastLookup} means the song has never been looked up and is therefore
   *           treated as older than any non-null value.
   *       <li>{@link JobStatus#PENDING PENDING} lookups are ordered before {@link JobStatus#DONE}
   *           lookups.
   *     </ul>
   *
   * @apiNote This method can be used to peek at the processing queue if called without a
   *     transaction, or via a read-only transaction. To claim the jobs for processing, this method
   *     must be called from within a write transaction, and must be marked as {@link
   *     JobStatus#PROCESSING claimed}.
   * @implNote A native query is used because the use of {@link Sort.Order#nullsFirst()} will cause
   *     the exception, {@code Applying Null Precedence using Criteria Queries is not yet supported}
   *     in the current version of SpringBoot ({@code 3.4.2}). The default null precedence for
   *     ascending order sorts in PostgreSQL is nulls last, so we need to specify this explicitly.
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

  /**
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  Collection<SongLookup> findAllByStatusAndLastModifiedBefore(JobStatus status, Instant instant);
}
