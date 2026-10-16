package com.bobo.storage.web.api.song;

import com.bobo.storage.core.song.Song;
import com.bobo.storage.web.semantic.ResponseSchema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

/**
 * {@link JsonInclude.Include#NON_ABSENT} so that {@code Optional} fields are only included in the
 * response if they have a value.
 *
 * <p>TODO Consider defining this globally.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public record SongResponse(
    @NotNull Integer id,
    @NotNull @Schema(format = "uri") String url,
    Optional<String> title,
    Optional<String> artist,
    @Schema(format = "uri") @JsonProperty("thumbnail_url") Optional<String> thumbnailUrl)
    implements ResponseSchema {

  public SongResponse(Song song) {
    this(song.getId(), song.getUrl(), song.getTitle(), song.getArtist(), song.getThumbnailUrl());
  }
}
