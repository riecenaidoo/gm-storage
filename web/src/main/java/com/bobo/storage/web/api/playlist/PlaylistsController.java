package com.bobo.storage.web.api.playlist;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.web.api.RequestConstraintViolationException;
import com.bobo.storage.web.api.ResourceNotFoundException;
import com.bobo.storage.web.semantic.ResourceController;
import java.net.URI;
import java.util.Collection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ResourceController
class PlaylistsController implements PlaylistAPI {

  private final PlaylistService playlists;

  PlaylistsController(PlaylistService playlists) {
    this.playlists = playlists;
  }

  @Override
  public ResponseEntity<PlaylistResponse> createPlaylist(PlaylistCreateRequest request) {
    Playlist playlist = request.toCreate();
    playlist = playlists.add(playlist);
    PlaylistResponse response = new PlaylistResponse(playlist);
    URI resource =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .pathSegment(String.valueOf(playlist.getId()))
            .build()
            .toUri();
    return ResponseEntity.created(resource).body(response);
  }

  @Override
  public ResponseEntity<PlaylistResponse[]> readPlaylists(PlaylistFilterQuery filters) {
    Collection<Playlist> playlists;
    if (filters.title().isPresent()) {
      String search = filters.title().get();
      if (search.isBlank()) {
        throw new RequestConstraintViolationException(
            "The 'title' parameter must not be blank if provided.");
      }
      playlists = this.playlists.searchByName(search);
    } else {
      playlists = this.playlists.get();
    }
    PlaylistResponse[] response =
        playlists.stream().map(PlaylistResponse::new).toArray(PlaylistResponse[]::new);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<PlaylistResponse> readPlaylist(int id) {
    Playlist playlist =
        playlists.find(id).orElseThrow(() -> new ResourceNotFoundException(Playlist.class, id));
    PlaylistResponse response = new PlaylistResponse(playlist);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<PlaylistResponse> updatePlaylist(int id, PlaylistPatchRequest request) {
    Playlist playlist =
        playlists.find(id).orElseThrow(() -> new ResourceNotFoundException(Playlist.class, id));
    request.patch(playlist);
    playlist = playlists.update(playlist);
    PlaylistResponse response = new PlaylistResponse(playlist);
    return ResponseEntity.ok(response);
  }

  @Override
  public ResponseEntity<Void> deletePlaylist(int id) {
    Playlist playlist =
        playlists.find(id).orElseThrow(() -> new ResourceNotFoundException(Playlist.class, id));
    playlists.delete(playlist);
    return ResponseEntity.noContent().build();
  }
}
