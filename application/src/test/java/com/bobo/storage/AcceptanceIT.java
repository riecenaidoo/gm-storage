package com.bobo.storage;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bobo.semantic.IntegrationTest;
import com.bobo.semantic.TestInfrastructure;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistMother;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMother;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.web.api.v2.playlist.song.PlaylistSongCreateRequest;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@IntegrationTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(classes = App.class)
@Testcontainers
public class AcceptanceIT {

  @Container @ServiceConnection
  private static final JdbcDatabaseContainer<?> database = TestInfrastructure.getDatabase();

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
    @DisplayName("A Song can be added to a Playlist")
    void existingSongIntoPlaylist() throws Exception {
      // Given
      Song existingSong = songs.add(new SongMother(random).get());

      PlaylistSongCreateRequest request = new PlaylistSongCreateRequest(existingSong.getId());
      String requestPayload = mapper.writeValueAsString(request);

      // When
      mvc.perform(
              MockMvcRequestBuilders.post("/api/v2/playlists/{playlistId}/songs", playlist.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(requestPayload))
          // Then
          .andExpect(status().isCreated());
    }
  }
}
