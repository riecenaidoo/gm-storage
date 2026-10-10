package com.bobo.storage.web.api.playlist.song;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.song.Song;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/playlists/{playlistId}/songs")
public interface PlaylistSongAPI {

  /** Add a {@link Song} into a {@link Playlist}. */
  @PostMapping
  ResponseEntity<PlaylistSongResponse> createPlaylistSong(
      @PathVariable("playlistId") int playlistId, @RequestBody int songId);

  @GetMapping
  ResponseEntity<PlaylistSongResponse[]> readPlaylistSongs(
      @PathVariable("playlistId") int playlistId);

  @DeleteMapping("/{songId}")
  ResponseEntity<Void> removePlaylistSong(
      @PathVariable("playlistId") int playlistId, @PathVariable("songId") int songId);
}
