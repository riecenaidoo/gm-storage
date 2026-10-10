package com.bobo.storage.web.api.playlist.song;

import com.bobo.storage.core.playlist.song.PlaylistSong;
import com.bobo.storage.web.api.song.SongResponse;

public record PlaylistSongResponse(Integer id, SongResponse songResponse) {

  public PlaylistSongResponse(PlaylistSong playlistSong) {
    this(playlistSong.getId(), new SongResponse(playlistSong.getSong()));
  }
}
