package com.bobo.storage.web.api.v2.song;

import com.bobo.storage.core.song.Song;
import com.bobo.storage.web.semantic.CreateRequest;

public record SongCreateRequest(String url) implements CreateRequest<Song> {

  @Override
  public Song toCreate() {
    return new Song(url);
  }
}
