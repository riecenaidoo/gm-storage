package com.bobo.storage.web.api.playlist;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.web.semantic.CreateRequest;

public record PlaylistCreateRequest(String title) implements CreateRequest<Playlist> {

  @Override
  public Playlist toCreate() {
    return new Playlist(title);
  }
}
