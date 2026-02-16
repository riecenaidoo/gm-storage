package com.bobo.storage.web.api.v2.request;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.web.semantic.PatchRequest;
import java.util.Optional;

public record PlaylistsPatchRequest(Optional<String> title) implements PatchRequest<Playlist> {

  @Override
  public Playlist patch(Playlist playlist) {
    title.ifPresent(playlist::setName);

    return playlist;
  }
}
