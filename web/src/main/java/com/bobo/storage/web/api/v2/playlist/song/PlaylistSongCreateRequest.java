package com.bobo.storage.web.api.v2.playlist.song;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.core.playlist.song.PlaylistSong;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.web.api.v2.ResourceNotFoundException;
import com.bobo.storage.web.semantic.CreateRequest;

/**
 * @param songId {@link Song}
 * @implSpec {@link CreateRequest}
 */
public record PlaylistSongCreateRequest(Integer songId) {

  public PlaylistSong toCreate(Integer playlistId, PlaylistService playlists, SongService songs) {
    Playlist playlist =
        playlists
            .find(playlistId)
            .orElseThrow(() -> new ResourceNotFoundException(Playlist.class, playlistId));
    Song song =
        songs.find(songId).orElseThrow(() -> new ResourceNotFoundException(Song.class, songId));

    return new PlaylistSong(playlist, song);
  }
}
