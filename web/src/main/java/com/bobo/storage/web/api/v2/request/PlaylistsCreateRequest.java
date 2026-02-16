package com.bobo.storage.web.api.v2.request;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.web.semantic.CreateRequest;

public record PlaylistsCreateRequest(String title) implements CreateRequest<Playlist> {

  @Override
  public Playlist toCreate() {
    return new Playlist(title);
  }
}
