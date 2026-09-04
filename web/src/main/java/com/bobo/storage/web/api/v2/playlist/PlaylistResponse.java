package com.bobo.storage.web.api.v2.playlist;

import com.bobo.storage.core.playlist.Playlist;

public record PlaylistResponse(Integer id, String title) {

  public PlaylistResponse(Playlist playlist) {
    this(playlist.getId(), playlist.getName());
  }
}
