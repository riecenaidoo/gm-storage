package com.bobo.storage.web.api.song.tag;

import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.core.song.tag.SongTag;
import com.bobo.storage.core.song.tag.SongTagService;
import com.bobo.storage.core.tag.Tag;
import com.bobo.storage.core.tag.TagService;
import com.bobo.storage.web.api.ResourceNotFoundException;
import com.bobo.storage.web.semantic.ResourceController;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ResourceController
class SongTagController implements SongTagAPI {

  private final SongTagService songTags;

  private final SongService songs;

  private final TagService tags;

  SongTagController(SongTagService songTags, SongService songs, TagService tags) {
    this.songTags = songTags;
    this.songs = songs;
    this.tags = tags;
  }

  @Override
  public ResponseEntity<@Valid SongTagResponse> createTag(int songId, SongTagPostRequest request) {
    Song song =
        songs.find(songId).orElseThrow(() -> new ResourceNotFoundException(Song.class, songId));
    Tag tag =
        tags.find(request.tagId())
            .orElseThrow(() -> new ResourceNotFoundException(Tag.class, request.tagId()));

    SongTag songTag = new SongTag(song, tag);
    songTag = songTags.add(songTag);
    SongTagResponse response = new SongTagResponse(songTag);
    URI resource =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .pathSegment(String.valueOf(songTag.getId()))
            .build()
            .toUri();
    return ResponseEntity.created(resource).body(response);
  }
}
