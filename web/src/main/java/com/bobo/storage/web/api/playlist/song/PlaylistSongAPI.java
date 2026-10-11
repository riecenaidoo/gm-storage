package com.bobo.storage.web.api.playlist.song;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Playlist Songs")
@RequestMapping("/playlists/{playlistId}/songs")
interface PlaylistSongAPI {

  @PostMapping
  @ApiResponse(responseCode = "201")
  ResponseEntity<@Valid PlaylistSongResponse> createPlaylistSong(
      @PathVariable("playlistId") int playlistId,
      @Valid @RequestBody PlaylistSongPostRequest request);

  @GetMapping
  ResponseEntity<@Valid PlaylistSongResponse[]> readPlaylistSongs(
      @PathVariable("playlistId") int playlistId);

  @DeleteMapping("/{id}")
  @ApiResponse(responseCode = "204")
  ResponseEntity<Void> removePlaylistSong(
      @PathVariable("playlistId") int playlistId, @PathVariable("id") int id);
}
