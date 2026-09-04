package com.bobo.storage.core.playlist.song;

import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMigration;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @implNote Extracted from {@link PlaylistSongService} to avoid circular dependencies.
 */
@Service
class PlaylistSongMigration implements SongMigration {

  private static final Logger log = LoggerFactory.getLogger(PlaylistSongMigration.class);

  private final PlaylistSongRepository playlistSongs;

  PlaylistSongMigration(PlaylistSongRepository playlistSongs) {
    this.playlistSongs = playlistSongs;
  }

  @Override
  @Transactional
  public void migrate(Song from, Song to) {
    Collection<PlaylistSong> songsToTransfer = playlistSongs.findAllBySong(from);
    if (songsToTransfer.isEmpty()) {
      log.trace("PlaylistSong#Migration: No PlaylistSongs associated with {}.", from.log());
      return;
    }
    songsToTransfer.forEach(song -> song.setSong(to));
    playlistSongs.saveAll(songsToTransfer);
    log.info(
        "PlaylistSong#Migration: {} migrated from {} to {}.",
        DomainEntity.log(songsToTransfer),
        from.log(),
        to.log());
  }
}
