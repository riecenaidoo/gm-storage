package com.bobo.storage;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bobo.semantic.IntegrationTest;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistMother;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMother;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.web.api.v2.request.SongsCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@IntegrationTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(classes = App.class)
public class AcceptanceIT {

  private final PlaylistService playlists;

  private final SongService songs;

  // Test Utilities

  private final MockMvc mvc;

  private final ObjectMapper mapper;

  private final Random random = new Random();

  @Autowired
  public AcceptanceIT(
      PlaylistService playlists, SongService songs, MockMvc mvc, ObjectMapper mapper) {
    this.playlists = playlists;
    this.songs = songs;
    this.mvc = mvc;
    this.mapper = mapper;
  }

  @Nested
  class ExistingPlaylist {

    private Playlist playlist;

    @BeforeEach
    void given() {
      Playlist playlist = new PlaylistMother(random).get();
      this.playlist = playlists.add(playlist);
    }

    @Test
    @DisplayName("A new Song can be added via a Playlist")
    void newSongViaPlaylist() throws Exception {
      // Given
      String validUrl = new SongMother(random).withUrls().get().getUrl();
      SongsCreateRequest request = new SongsCreateRequest(validUrl);
      String requestPayload = mapper.writeValueAsString(request);

      // When
      mvc.perform(
              MockMvcRequestBuilders.post("/api/v2/playlists/{playlist_id}/songs", playlist.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestPayload))
          // Then
          .andExpect(status().isCreated());
    }

    /**
     * @implNote This failed due to the constraint that a Song#url must be unique. If we let the ORM
     *     handle adding the Song, it will succeed if it is a unique URL but fail otherwise.
     *     Previously we were correctly adding the Song via the service before adding it into the
     *     Playlist, but during a refactor a while back we regressed.
     */
    @Test
    @DisplayName("An existing Song can be added to a Playlist")
    void existingSongIntoPlaylist() throws Exception {
      // Given
      Song existingSong = new SongMother(random).get();
      existingSong = songs.add(existingSong);

      String validUrl = existingSong.getUrl();
      SongsCreateRequest request = new SongsCreateRequest(validUrl);
      String requestPayload = mapper.writeValueAsString(request);

      // When
      mvc.perform(
              MockMvcRequestBuilders.post("/api/v2/playlists/{playlist_id}/songs", playlist.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestPayload))
          // Then
          .andExpect(status().isCreated());
    }
  }
}
