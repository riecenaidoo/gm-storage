package com.bobo.storage.web.api.playlist.song;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.core.playlist.song.PlaylistSong;
import com.bobo.storage.core.playlist.song.PlaylistSongService;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.web.api.ResourceNotFoundException;
import com.bobo.storage.web.api.SubResourceMismatchException;
import java.net.URI;
import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class PlaylistSongsController implements PlaylistSongAPI {

  private final PlaylistSongService playlistSongs;

  private final PlaylistService playlists;

  private final SongService songs;

  public PlaylistSongsController(
      PlaylistService playlists, PlaylistSongService playlistSongs, SongService songs) {
    this.playlistSongs = playlistSongs;
    this.playlists = playlists;
    this.songs = songs;
  }

  @Override
  public ResponseEntity<PlaylistSongResponse> createPlaylistSong(int playlistId, int songId) {
    Playlist playlist =
        playlists
            .find(playlistId)
            .orElseThrow(() -> new ResourceNotFoundException(Playlist.class, playlistId));
    Song song =
        songs.find(songId).orElseThrow(() -> new ResourceNotFoundException(Song.class, songId));

    PlaylistSong playlistSong = new PlaylistSong(playlist, song);

    playlistSong = playlistSongs.add(playlistSong);
    PlaylistSongResponse response = new PlaylistSongResponse(playlistSong);
    URI resource =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path(String.format("/%d", playlistSong.getId()))
            .build()
            .toUri();
    return ResponseEntity.created(resource).body(response);
  }

  @Override
  public ResponseEntity<PlaylistSongResponse[]> readPlaylistSongs(int playlistId) {
    Playlist playlist =
        playlists
            .find(playlistId)
            .orElseThrow(() -> new ResourceNotFoundException(Playlist.class, playlistId));
    Collection<PlaylistSong> playlistSongs = this.playlistSongs.getFromPlaylist(playlist);
    PlaylistSongResponse[] response =
        playlistSongs.stream().map(PlaylistSongResponse::new).toArray(PlaylistSongResponse[]::new);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> removePlaylistSong(int playlistId, int songId) {
    Playlist playlist =
        playlists
            .find(playlistId)
            .orElseThrow(() -> new ResourceNotFoundException(Playlist.class, playlistId));
    PlaylistSong playlistSong =
        playlistSongs
            .find(songId)
            .orElseThrow(() -> new ResourceNotFoundException(PlaylistSong.class, songId));
    if (!TechnicalID.same(playlist, playlistSong.getPlaylist())) {
      throw new SubResourceMismatchException(playlist, playlistSong);
    }
    playlistSongs.delete(playlistSong);
    return ResponseEntity.noContent().build();
  }
}
