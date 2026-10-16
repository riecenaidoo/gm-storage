package com.bobo.storage.web.api.playlist.song;

import com.bobo.storage.core.playlist.song.PlaylistSong;
import com.bobo.storage.web.api.song.SongResponse;
import com.bobo.storage.web.semantic.ResponseSchema;
import jakarta.validation.constraints.NotNull;

public record PlaylistSongResponse(@NotNull Integer id, @NotNull SongResponse song)
    implements ResponseSchema {

  public PlaylistSongResponse(PlaylistSong playlistSong) {
    this(playlistSong.getId(), new SongResponse(playlistSong.getSong()));
  }
}
