package com.bobo.storage.web.api.playlist;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.web.semantic.CreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record PlaylistCreateRequest(@NotNull @NotBlank String title) implements CreateRequest<Playlist> {

  @Override
  public Playlist toCreate() {
    return new Playlist(title);
  }
}
