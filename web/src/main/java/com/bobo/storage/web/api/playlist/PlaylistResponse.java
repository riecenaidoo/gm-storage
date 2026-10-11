package com.bobo.storage.web.api.playlist;

import com.bobo.storage.core.playlist.Playlist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaylistResponse(@NotNull Integer id, @NotNull @NotBlank String title) {

  public PlaylistResponse(Playlist playlist) {
    this(playlist.getId(), playlist.getName());
  }
}
