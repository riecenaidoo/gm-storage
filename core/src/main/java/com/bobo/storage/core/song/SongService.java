package com.bobo.storage.core.song;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.semantic.CoreService;
import com.bobo.storage.core.semantic.Create;
import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.semantic.EntityService;
import com.bobo.storage.core.semantic.Read;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@CoreService
public class SongService implements EntityService<Song>, Create<Song>, Read<Song> {

  private static final Logger log = LoggerFactory.getLogger(SongService.class);

  private final SongRepository songs;

  private final Collection<SongMigration> migrations;

  SongService(SongRepository songs, Collection<SongMigration> migrations) {
    this.songs = songs;
    this.migrations = migrations;
  }

  /**
   * Songs are uniquely identified by their {@code url}; only one {@link Song} with a given {@code
   * url} can exist in the system at any time.
   *
   * @implSpec {@link Create#add(DomainEntity)}
   */
  @Override
  @Transactional
  public Song add(Song song) {
    if (Objects.nonNull(song.getId())) throw new IllegalArgumentException();

    return songs.findByUrl(song.getUrl()).orElseGet(() -> songs.save(song));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Song> find(int id) {
    return songs.findById(id);
  }

  /**
   * @return a {@link Collection} of all {@link Song} resources that are eligible for a lookup;
   *     never null.
   * @see Song#lookedUp()
   * @see LookupService
   * @apiNote In future, we will accept a {@code limit} parameter to control the return size.
   * @implSpec {@link Read#get()}
   * @implNote In future, we will mark songs as eligible for a lookup, after a configurable period
   *     has past since their last lookup. Those that have never been looked up will be given
   *     priority.
   *     <p>Currently, the expected number of {@code Song} entities requiring lookup at any given
   *     time is small (typically 0-4), and the total table size remains well below 1000 entries.
   *     Because of this, batch processing or paging is not implemented. However, if the dataset
   *     grows significantly, it will be necessary to introduce paging or limit batch size to avoid
   *     performance or memory issues.
   *     <p>TODO: Refer to the <a
   *     href="https://www.postgresql.org/docs/current/indexes-partial.html">Partial Index</a>
   *     documentation for PostgresSQL to create an index targeting null values for last lookups, or
   *     explore other optimization strategies to improve query performance.
   */
  @Transactional(readOnly = true)
  public Collection<Song> getLookupCandidates() {
    return songs.findAllByLastLookupIsNull();
  }

  /**
   * Updates a {@link Song}, deduplicating by {@code url} if necessary.
   *
   * @implNote If the {@code url} already exists within the system, we can safely discard any other
   *     partial updates to the {@link Song} and perform a {@link SongMigration} because the data
   *     associated with a {@link Song} can be deterministically derived by the system.
   * @see #add(Song)
   */
  @Transactional
  public void updateSong(Song song) {
    if (song.getId() == null) throw new IllegalArgumentException();

    Optional<Song> existingSong = songs.findByUrl(song.getUrl());
    if (existingSong.isPresent() && !TechnicalID.same(existingSong.get(), song)) {
      log.info(
          "Song#Update: {} URL is already mapped by {}. Performing de-duplication. Its references"
              + " will be migrated to the existing Song, and it will be removed.",
          song.log(),
          existingSong.get().log());
      migrations.forEach(migration -> migration.migrate(song, existingSong.get()));
      songs.delete(song);
    } else {
      songs.save(song);
    }
  }
}
