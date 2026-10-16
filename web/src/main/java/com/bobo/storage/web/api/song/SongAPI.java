package com.bobo.storage.web.api.song;

import com.bobo.storage.web.semantic.ResourceAPI;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Songs")
@RequestMapping("/songs")
interface SongAPI extends ResourceAPI {

  @PostMapping
  @ApiResponse(responseCode = "201")
  ResponseEntity<@Valid SongResponse> createSong(@Valid @RequestBody SongCreateRequest request);
}
