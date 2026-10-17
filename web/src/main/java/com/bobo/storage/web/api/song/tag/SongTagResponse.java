package com.bobo.storage.web.api.song.tag;

import com.bobo.storage.core.song.tag.SongTag;
import com.bobo.storage.web.api.tag.TagResponse;
import com.bobo.storage.web.semantic.ResponseSchema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SongTagResponse(
    @NotNull Integer id, @NotNull Integer songId, @Valid @NotNull TagResponse tag)
    implements ResponseSchema {

  public SongTagResponse(SongTag songTag) {
    this(songTag.getId(), songTag.getSong().getId(), new TagResponse(songTag.getTag()));
  }
}
