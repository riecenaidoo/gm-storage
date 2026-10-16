package com.bobo.storage.web.api.song;

import com.bobo.storage.core.song.Song;
import com.bobo.storage.web.semantic.CreateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

record SongCreateRequest(@NotNull @Schema(format = "uri") String url)
    implements CreateRequest<Song> {

  @Override
  public Song toCreate() {
    return new Song(url);
  }
}
