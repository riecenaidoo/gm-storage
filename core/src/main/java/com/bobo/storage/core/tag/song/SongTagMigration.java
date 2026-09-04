package com.bobo.storage.core.tag.song;

import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMigration;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SongTagMigration implements SongMigration {

  private static final Logger log = LoggerFactory.getLogger(SongTagMigration.class);

  private final SongTagRepository songTags;

  SongTagMigration(SongTagRepository songTags) {
    this.songTags = songTags;
  }

  @Override
  @Transactional
  public void migrate(Song from, Song to) {
    Collection<SongTag> tagsToTransfer = songTags.findAllBySong(from);
    if (tagsToTransfer.isEmpty()) {
      log.trace("SongTag#Migration: No SongTags associated with {}.", from.log());
      return;
    }
    tagsToTransfer.forEach(tag -> tag.setSong(to));
    songTags.saveAll(tagsToTransfer);
    log.info(
        "SongTag#Migration: {} migrated from {} to {}.",
        DomainEntity.log(tagsToTransfer),
        from.log(),
        to.log());
  }
}
