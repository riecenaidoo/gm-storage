package com.bobo.storage.web.api.song;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/songs")
public interface SongAPI {

  @PostMapping
  ResponseEntity<SongResponse> createSong(@RequestBody SongCreateRequest request);
}
