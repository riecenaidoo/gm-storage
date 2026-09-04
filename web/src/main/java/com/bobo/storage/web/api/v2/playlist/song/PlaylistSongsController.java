package com.bobo.storage.web.api.v2.playlist.song;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.core.playlist.song.PlaylistSong;
import com.bobo.storage.core.playlist.song.PlaylistSongService;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.web.api.v2.ResourceNotFoundException;
import com.bobo.storage.web.api.v2.SubresourceMismatchException;
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

  private final SongService songs;

  public PlaylistSongsController(
      PlaylistService playlists, PlaylistSongService playlistSongs, SongService songs) {
    this.playlistSongs = playlistSongs;
    this.playlists = playlists;
    this.songs = songs;
  }

  /** Add a {@link Song} into a {@link Playlist}. */
  @PostMapping
  public ResponseEntity<PlaylistSongResponse> createPlaylistSong(
      @PathVariable int playlistId, @RequestBody PlaylistSongCreateRequest request) {
    PlaylistSong playlistSong = request.toCreate(playlistId, playlists, songs);
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

  @DeleteMapping("/{songId}")
  public ResponseEntity<Void> removePlaylistSong(
      @PathVariable int playlistId, @PathVariable int songId) {
    Playlist playlist =
        playlists
            .find(playlistId)
            .orElseThrow(() -> new ResourceNotFoundException(Playlist.class, playlistId));
    PlaylistSong playlistSong =
        playlistSongs
            .find(songId)
            .orElseThrow(() -> new ResourceNotFoundException(PlaylistSong.class, songId));
    if (!TechnicalID.same(playlist, playlistSong.getPlaylist())) {
      throw new SubresourceMismatchException(playlist, playlistSong);
    }
    playlistSongs.delete(playlistSong);
    return ResponseEntity.noContent().build();
  }
}
