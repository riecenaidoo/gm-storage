package com.bobo.storage.web.api.v2.request;

import com.bobo.storage.core.song.Song;
import com.bobo.storage.web.semantic.CreateRequest;

public record SongsCreateRequest(String url) implements CreateRequest<Song> {

  @Override
  public Song toCreate() {
    return new Song(url);
  }
}
