package com.bobo.storage.web.api.playlist;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.web.semantic.PatchRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record PlaylistPatchRequest(@NotNull @NotBlank String title) implements PatchRequest<Playlist> {

  @Override
  public Playlist patch(Playlist playlist) {
    playlist.setName(title);

    return playlist;
  }
}
