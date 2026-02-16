package com.bobo.storage.web.api.v2.controller;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.core.playlist.song.PlaylistSong;
import com.bobo.storage.core.playlist.song.PlaylistSongService;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.web.api.v2.request.SongsCreateRequest;
import com.bobo.storage.web.api.v2.response.PlaylistSongResponse;
import com.bobo.storage.web.semantic.ResourceController;
import java.net.URI;
import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequestMapping("/api/v2/playlists/{playlistId}/songs")
@ResourceController(resource = PlaylistSong.class, respondsWith = PlaylistSongResponse.class)
public class PlaylistSongsController {

  private final PlaylistSongService playlistSongs;

  private final PlaylistService playlists;

  public PlaylistSongsController(PlaylistService playlists, PlaylistSongService playlistSongs) {
    this.playlistSongs = playlistSongs;
    this.playlists = playlists;
  }

  /**
   * Create a {@link Song} within a {@link Playlist}.
   *
   * @param playlistId of the {@code Playlist} to create the {@code Song} within.
   * @param request to create a {@code Song}.
   */
  @PostMapping
  public ResponseEntity<PlaylistSongResponse> createPlaylistSong(
      @PathVariable int playlistId, @RequestBody SongsCreateRequest request) {
    Playlist playlist =
        playlists
            .find(playlistId)
            .orElseThrow(() -> new ResourceNotFoundException(Playlist.class, playlistId));
    PlaylistSong playlistSong = new PlaylistSong(playlist, request.toCreate());
    playlistSong = playlistSongs.add(playlistSong);
    PlaylistSongResponse response = new PlaylistSongResponse(playlistSong);
    URI resource =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path(String.format("/%d", playlistSong.getId()))
            .build()
            .toUri();
    return ResponseEntity.created(resource).body(response);
  }

  @GetMapping
  public ResponseEntity<PlaylistSongResponse[]> readPlaylistSongs(@PathVariable int playlistId) {
    Playlist playlist =
        playlists
            .find(playlistId)
            .orElseThrow(() -> new ResourceNotFoundException(Playlist.class, playlistId));
    Collection<PlaylistSong> playlistSongs = this.playlistSongs.getFromPlaylist(playlist);
    PlaylistSongResponse[] response =
        playlistSongs.stream().map(PlaylistSongResponse::new).toArray(PlaylistSongResponse[]::new);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> removePlaylistSong(
      @PathVariable int playlistId, @PathVariable int id) {
    Playlist playlist =
        playlists
            .find(playlistId)
            .orElseThrow(() -> new ResourceNotFoundException(Playlist.class, playlistId));
    PlaylistSong playlistSong =
        playlistSongs
            .find(id)
            .orElseThrow(() -> new ResourceNotFoundException(PlaylistSong.class, id));
    if (!TechnicalID.same(playlist, playlistSong.getPlaylist())) {
      throw new SubresourceMismatchException(playlist, playlistSong);
    }
    playlistSongs.delete(playlistSong);
    return ResponseEntity.noContent().build();
  }
}
