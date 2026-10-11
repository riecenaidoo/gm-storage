package com.bobo.storage.web.api.playlist.song;

import com.bobo.storage.core.playlist.song.PlaylistSong;
import com.bobo.storage.web.api.song.SongResponse;
import jakarta.validation.constraints.NotNull;

public record PlaylistSongResponse(@NotNull Integer id, @NotNull SongResponse song) {

  public PlaylistSongResponse(PlaylistSong playlistSong) {
    this(playlistSong.getId(), new SongResponse(playlistSong.getSong()));
  }
}
