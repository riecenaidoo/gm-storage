package com.bobo.storage.web.api.v2.song;

import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.web.semantic.ResourceController;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequestMapping("/api/v2/songs")
@ResourceController(resource = Song.class, respondsWith = SongResponse.class)
public class SongsController {

  private final SongService songs;

  public SongsController(SongService songs) {
    this.songs = songs;
  }

  @PostMapping
  public ResponseEntity<SongResponse> createSong(@RequestBody SongCreateRequest request) {
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
