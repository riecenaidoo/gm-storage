package com.bobo.storage.core.song;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.semantic.CoreService;
import com.bobo.storage.core.semantic.Create;
import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.semantic.EntityService;
import com.bobo.storage.core.semantic.Read;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@CoreService
public class SongService implements EntityService<Song>, Create<Song>, Read<Song> {

  private static final Logger log = LoggerFactory.getLogger(SongService.class);

  private final SongRepository songs;

  private final SongLookupRepository lookups;

  private final Collection<SongMigration> migrations;

  SongService(
      SongRepository songs, SongLookupRepository lookups, Collection<SongMigration> migrations) {
    this.songs = songs;
    this.lookups = lookups;
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

    Optional<Song> existingSong = songs.findByUrl(song.getUrl());
    if (existingSong.isPresent()) {
      return existingSong.get();
    }

    song = songs.save(song);
    lookups.save(new SongLookup(song));
    return song;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Song> find(int id) {
    return songs.findById(id);
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

      lookups.findBySong(song).ifPresent(lookups::delete);
      songs.delete(song);
    } else {
      songs.save(song);
    }
  }

  /**
   * Ensure that system-wide {@link Song} invariants have been maintained.
   *
   * <p>Verifies that all {@link Song} have an associated {@link SongLookup} job, creating them if
   * they are missing.
   *
   * @apiNote This is provided to support bulk loading of data directly into the database.
   */
  @Transactional
  public void verify() {
    if (songs.count() == lookups.count()) {
      log.trace("Ma'at: There is order.");
      return;
    }

    Collection<Song> joblessSongs = songs.findJoblessSongs();
    List<SongLookup> jobs = joblessSongs.stream().map(SongLookup::new).toList();
    lookups.saveAll(jobs);

    log.warn(
        "Ma'at: An imbalance was found amongst {}. Order has been restored.",
        DomainEntity.log(joblessSongs));
  }
}
