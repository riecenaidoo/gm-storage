package com.bobo.storage.core.song.tag;

import com.bobo.storage.core.semantic.Create;
import com.bobo.storage.core.semantic.EntityService;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class SongTagService implements EntityService<SongTag>, Create<SongTag> {

  private final SongTagRepository songTags;

  SongTagService(SongTagRepository songTags) {
    this.songTags = songTags;
  }

  @Override
  public SongTag add(SongTag songTag) throws IllegalArgumentException {
    if (Objects.nonNull(songTag.getId())) throw new IllegalArgumentException();
    return songTags.save(songTag);
  }
}
