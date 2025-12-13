package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.DomainEntity;

/**
 * Provides a strategy for handling a {@link Song} migration, which is performed during {@link Song}
 * deduplication.
 *
 * @apiNote Every {@link DomainEntity} associated with a {@link Song} should provide a strategy for
 * handling {@link Song} migrations, or be configured to cascade delete itself with its associated
 * {@link Song}.
 */
public interface SongMigration {

  /**
   * Migrate associations to a {@link Song}.
   *
   * @param from {@link Song} to transfer from.
   * @param to {@link Song} to transfer to.
   */
  void migrate(Song from, Song to);

}
