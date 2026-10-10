package com.bobo.storage.web.api.playlist.song;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bobo.semantic.IntegrationTest;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistMother;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.core.playlist.song.PlaylistSong;
import com.bobo.storage.core.playlist.song.PlaylistSongMother;
import com.bobo.storage.core.playlist.song.PlaylistSongService;
import com.bobo.storage.core.semantic.EntityMother;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMother;
import com.bobo.storage.core.song.SongService;
import com.bobo.storage.web.WebTestApplication;
import com.bobo.storage.web.api.ControllerExceptionHandler;
import com.bobo.storage.web.api.song.SongResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@IntegrationTest({
  PlaylistSongsController.class,
  ObjectMapper.class,
  ControllerExceptionHandler.class
})
@WebMvcTest(PlaylistSongsController.class)
class PlaylistSongsControllerIT {

  // Mock Dependencies

  @MockitoBean private PlaylistSongService playlistSongs;

  @MockitoBean private PlaylistService playlists;

  @MockitoBean private SongService songs;

  // Test Utilities

  private final MockMvc mvc;

  private final ObjectMapper mapper;

  /**
   * To be used for creating arbitrary values that will <b>never</b> change during the scope of the
   * test they were created within.
   *
   * <p>Mark such values {@code final} where possible to declare the intention.
   */
  private final Random random = new Random();

  @Autowired
  PlaylistSongsControllerIT(MockMvc mvc, ObjectMapper mapper) {
    this.mvc = mvc;
    this.mapper = mapper;
  }

  /**
   * @see PlaylistSongsController#createPlaylistSong(int, int)
   */
  @DisplayName("POST /playlists/{playlistId}/songs")
  @Test
  void createPlaylistSong() throws Exception {
    // Given
    Playlist playlist = new PlaylistMother(random).withIds().get();
    Song song = new SongMother(random).withIds().get();

    int request = song.getId();
    String requestPayload = mapper.writeValueAsString(request);

    final int id = random.nextInt(1, 101);
    PlaylistSongResponse expectedResponse = new PlaylistSongResponse(id, new SongResponse(song));
    String expectedPayload = mapper.writeValueAsString(expectedResponse);
    String expectedURI =
        String.format(
            "%s/playlists/%d/songs/%d",
            WebTestApplication.testSchemeAuthority(), playlist.getId(), id);

    when(playlists.find(playlist.getId())).thenReturn(Optional.of(playlist));
    when(songs.find(song.getId())).thenReturn(Optional.of(song));
    when(playlistSongs.add(any(PlaylistSong.class)))
        .thenAnswer(
            invocation -> {
              PlaylistSong playlistSong = invocation.getArgument(0);
              return EntityMother.setId(playlistSong, id);
            });

    // When
    mvc.perform(
            post("/playlists/{playlistId}/songs", playlist.getId())
                .with(WebTestApplication::testSchemeAuthority)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestPayload))
        // Then
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", expectedURI))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedPayload));

    verify(playlistSongs, times(1)).add(any(PlaylistSong.class));
  }

  /**
   * @see PlaylistSongsController#readPlaylistSongs(int)
   */
  @Test
  void readPlaylistSongs() throws Exception {
    // Given
    Playlist playlist = new PlaylistMother(random).withIds().get();
    PlaylistSong[] allPlaylistSongs =
        new PlaylistSongMother(random)
            .withPlaylists(() -> playlist)
            .get(random.nextInt(1, 101))
            .toArray(PlaylistSong[]::new);
    PlaylistSongResponse[] expectedResponse =
        Arrays.stream(allPlaylistSongs)
            .map(PlaylistSongResponse::new)
            .toArray(PlaylistSongResponse[]::new);
    String expectedPayload = mapper.writeValueAsString(expectedResponse);

    // Stubbing
    when(playlists.find(playlist.getId())).thenReturn(Optional.of(playlist));
    when(playlistSongs.getFromPlaylist(playlist)).thenReturn(List.of(allPlaylistSongs));

    // When
    mvc.perform(get("/playlists/{playlistId}/songs", playlist.getId()))
        // Then
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedPayload));
  }

  /**
   * @see PlaylistSongsController#removePlaylistSong(int, int)
   */
  @Test
  @DisplayName("DELETE /playlists/{playlistId}/songs/{songId}")
  void removePlaylistSong() throws Exception {
    // Given
    PlaylistSong playlistSong = new PlaylistSongMother(random).withAll().get();
    int playlistId = playlistSong.getPlaylist().getId();
    int id = playlistSong.getId();

    // Stubbing
    when(playlists.find(playlistId)).thenReturn(Optional.of(playlistSong.getPlaylist()));
    when(playlistSongs.find(id)).thenReturn(Optional.of(playlistSong));

    // When
    mvc.perform(delete("/playlists/{playlistId}/songs/{songId}", playlistId, id))
        // Then
        .andExpect(status().isNoContent())
        .andExpect(header().doesNotExist("content-type"));

    verify(playlistSongs, times(1)).delete(playlistSong);
  }
}
