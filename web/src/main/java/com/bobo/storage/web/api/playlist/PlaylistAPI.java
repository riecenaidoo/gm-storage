package com.bobo.storage.web.api.playlist;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/playlists")
public interface PlaylistAPI {

  @PostMapping
  ResponseEntity<PlaylistResponse> createPlaylist(@RequestBody PlaylistCreateRequest request);

  /**
   * @return zero or more {@code Playlists}.
   */
  @GetMapping
  ResponseEntity<PlaylistResponse[]> readPlaylists(@ModelAttribute PlaylistFilterQuery filters);

  @GetMapping("/{id}")
  ResponseEntity<PlaylistResponse> readPlaylist(@PathVariable("id") int id);

  @PatchMapping("/{id}")
  ResponseEntity<PlaylistResponse> updatePlaylist(
      @PathVariable("id") int id, @RequestBody PlaylistPatchRequest request);

  @DeleteMapping("/{id}")
  ResponseEntity<Void> deletePlaylist(@PathVariable("id") int id);
}
