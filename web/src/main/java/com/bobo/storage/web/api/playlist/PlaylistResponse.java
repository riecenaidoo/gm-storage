package com.bobo.storage.web.api.playlist;

import com.bobo.storage.core.playlist.Playlist;

public record PlaylistResponse(Integer id, String title) {

  public PlaylistResponse(Playlist playlist) {
    this(playlist.getId(), playlist.getName());
  }
}
