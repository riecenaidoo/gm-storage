package com.bobo.storage.web.api.playlist;

import com.bobo.storage.web.semantic.ResourceAPI;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Playlists")
@RequestMapping("/playlists")
interface PlaylistAPI extends ResourceAPI {

  @PostMapping
  @ApiResponse(responseCode = "201")
  ResponseEntity<@Valid PlaylistResponse> createPlaylist(
      @Valid @RequestBody PlaylistCreateRequest request);

  @GetMapping
  ResponseEntity<@Valid PlaylistResponse[]> readPlaylists(
      @Valid @ParameterObject @ModelAttribute PlaylistFilterQuery filters);

  @GetMapping("/{id}")
  ResponseEntity<@Valid PlaylistResponse> readPlaylist(@PathVariable("id") int id);

  @PatchMapping("/{id}")
  ResponseEntity<@Valid PlaylistResponse> updatePlaylist(
      @PathVariable("id") int id, @Valid @RequestBody PlaylistPatchRequest request);

  @DeleteMapping("/{id}")
  @ApiResponse(responseCode = "204")
  ResponseEntity<Void> deletePlaylist(@PathVariable("id") int id);
}
