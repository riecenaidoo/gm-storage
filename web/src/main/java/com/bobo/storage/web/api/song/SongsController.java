package com.bobo.storage.web.api.song;

import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.web.semantic.ResourceController;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ResourceController
class SongsController implements SongAPI {

  private final SongService songs;

  SongsController(SongService songs) {
    this.songs = songs;
  }

  @Override
  public ResponseEntity<SongResponse> createSong(SongCreateRequest request) {
    Song song = request.toCreate();
    song = songs.add(song);
    SongResponse response = new SongResponse(song);
    URI resource =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .pathSegment(String.valueOf(song.getId()))
            .build()
            .toUri();
    return ResponseEntity.created(resource).body(response);
  }
}
