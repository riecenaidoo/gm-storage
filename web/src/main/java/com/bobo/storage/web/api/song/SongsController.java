package com.bobo.storage.web.api.song;

import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class SongsController implements SongAPI {

  private final SongService songs;

  public SongsController(SongService songs) {
    this.songs = songs;
  }

  @Override
  public ResponseEntity<SongResponse> createSong(SongCreateRequest request) {
    Song song = request.toCreate();
    song = songs.add(song);
    SongResponse response = new SongResponse(song);
    URI resource =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .path(String.format("/%d", song.getId()))
            .build()
            .toUri();
    return ResponseEntity.created(resource).body(response);
  }
}
