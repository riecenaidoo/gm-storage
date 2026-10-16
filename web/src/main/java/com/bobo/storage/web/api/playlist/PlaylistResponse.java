package com.bobo.storage.web.api.playlist;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.web.semantic.ResponseSchema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaylistResponse(@NotNull Integer id, @NotNull @NotBlank String title)
    implements ResponseSchema {

  public PlaylistResponse(Playlist playlist) {
    this(playlist.getId(), playlist.getName());
  }
}
