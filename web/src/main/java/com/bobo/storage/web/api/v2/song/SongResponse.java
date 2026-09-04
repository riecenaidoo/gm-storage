package com.bobo.storage.web.api.v2.song;

import com.bobo.storage.core.song.Song;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * {@link JsonInclude.Include#NON_ABSENT} so that {@code Optional} fields are only included in the
 * response if they have a value.
 *
 * <p>TODO Consider defining this globally.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record SongResponse(
    Integer id,
    String url,
    Optional<String> title,
    Optional<String> artist,
    @JsonProperty("thumbnail_url") Optional<String> thumbnailUrl) {

  public SongResponse(Song song) {
    this(
        song.getId(),
        song.getUrl(),
        song.getTitle(),
        song.getArtist(),
        song.getThumbnailUrl());
  }
}
