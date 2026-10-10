package com.bobo.storage.web.api.v2.playlist;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/playlists")
public interface PlaylistAPI {

  @PostMapping
  ResponseEntity<PlaylistResponse> createPlaylist(@RequestBody PlaylistCreateRequest request);

  /**
   * @param title an optional title fragment used as a case-insensitive search filter. If specified,
   *     must not be blank (see {@link String#isBlank()}).
   * @return zero or more {@code Playlists}.
   */
  @GetMapping
  ResponseEntity<PlaylistResponse[]> readPlaylists(@RequestParam("title") Optional<String> title);

  @GetMapping("/{id}")
  ResponseEntity<PlaylistResponse> readPlaylist(@PathVariable("id") int id);

  @PatchMapping("/{id}")
  ResponseEntity<PlaylistResponse> updatePlaylist(
      @PathVariable("id") int id, @RequestBody PlaylistPatchRequest request);

  @DeleteMapping("/{id}")
  ResponseEntity<Void> deletePlaylist(@PathVariable("id") int id);
}
