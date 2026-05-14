package com.bobo.storage.web.api.v2.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bobo.semantic.IntegrationTest;
import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistMother;
import com.bobo.storage.core.playlist.PlaylistService;
import com.bobo.storage.core.semantic.EntityMother;
import com.bobo.storage.web.WebTestApplication;
import com.bobo.storage.web.api.v2.request.PlaylistsCreateRequest;
import com.bobo.storage.web.api.v2.request.PlaylistsPatchRequest;
import com.bobo.storage.web.api.v2.response.PlaylistResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.ObjectMapper;

@IntegrationTest({PlaylistsController.class, ObjectMapper.class, ControllerExceptionHandler.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WebMvcTest(PlaylistsController.class)
class PlaylistsControllerIT {

  // Mock Dependencies

  @MockitoBean private PlaylistService playlists;

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
  PlaylistsControllerIT(MockMvc mvc, ObjectMapper mapper) {
    this.mvc = mvc;
    this.mapper = mapper;
  }

  /**
   * @see PlaylistsController#createPlaylist(PlaylistsCreateRequest)
   */
  @DisplayName("POST /playlists")
  @Test
  void createPlaylist() throws Exception {
    // Given
    String validName = new PlaylistMother(random).withNames().get().getName();
    PlaylistsCreateRequest request = new PlaylistsCreateRequest(validName);
    String requestPayload = mapper.writeValueAsString(request);

    final int id = random.nextInt(1, 101);
    PlaylistResponse expectedResponse =
        new PlaylistResponse(EntityMother.setId(request.toCreate(), id));
    String expectedPayload = mapper.writeValueAsString(expectedResponse);
    String expectedURI =
        String.format("%s/api/v2/playlists/%d", WebTestApplication.testSchemeAuthority(), id);

    // Stubbing
    when(playlists.add(any()))
        .thenAnswer(
            invocation -> {
              Playlist playlist = invocation.getArgument(0);
              return EntityMother.setId(playlist, id);
            });

    // When
    mvc.perform(
            post("/api/v2/playlists")
                .with(WebTestApplication::testSchemeAuthority)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestPayload))
        // Then
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", expectedURI))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedPayload));

    verify(playlists, times(1)).add(request.toCreate());
  }

  /**
   * @see PlaylistsController#readPlaylists(Optional)
   */
  @Nested
  class ReadPlaylists {

    /** The default behaviour is to read all {@code Playlists} when no filter is supplied. */
    @DisplayName("GET /playlists")
    @Test
    void readPlaylists() throws Exception {
      // Given
      Collection<Playlist> allPlaylists = new PlaylistMother(random).withAll().get(5).toList();

      PlaylistResponse[] expectedResponse =
          allPlaylists.stream().map(PlaylistResponse::new).toArray(PlaylistResponse[]::new);
      String expectedPayload = mapper.writeValueAsString(expectedResponse);

      // Stubbing
      when(playlists.get()).thenReturn(allPlaylists);

      // When
      mvc.perform(get("/api/v2/playlists"))
          // Then
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(content().json(expectedPayload));

      verify(playlists, times(1)).get();
      verify(playlists, times(0)).searchByName(anyString());
    }

    /**
     * The repository method is tested, we just want to ensure we are calling the correct query
     * method when we receive the filter parameter.
     */
    @DisplayName("GET /playlists?title=")
    @Test
    void filterByTitle() throws Exception {
      // Given
      String title = "Other";

      // Stubbing
      when(playlists.searchByName(anyString())).thenReturn(List.of());

      // When
      mvc.perform(get("/api/v2/playlists").param("title", title))
          // Then
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(content().json("[]"));

      verify(playlists, times(1)).searchByName(title);
      verify(playlists, times(0)).get();
    }

    @DisplayName("GET /playlists?title= (title must not be blank)")
    @ParameterizedTest()
    @ValueSource(strings = {"", " ", "\t", "\n", "    "})
    void filterByBlankTitle(String title) throws Exception {
      // Given
      Assertions.assertTrue(title.isBlank(), "Test Assumption Failed.");

      // When
      mvc.perform(get("/api/v2/playlists").param("title", title))
          // Then
          .andExpect(status().isBadRequest());
    }
  }

  /**
   * @see PlaylistsController#readPlaylist(int)
   */
  @DisplayName("GET /playlists/{id}")
  @Test
  void readPlaylist() throws Exception {
    // Given
    PlaylistMother mother = new PlaylistMother(random).withAll();
    Playlist playlist = mother.get();
    Integer id = playlist.getId();

    PlaylistResponse expectedResponse = new PlaylistResponse(playlist);
    String expectedPayload = mapper.writeValueAsString(expectedResponse);

    // Stubbing
    when(playlists.find(id)).thenReturn(Optional.of(playlist));

    // When
    mvc.perform(get("/api/v2/playlists/{id}", id))
        // Then
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedPayload));
  }

  /**
   * @see PlaylistsController#updatePlaylist(int, PlaylistsPatchRequest)
   */
  @DisplayName("PATCH /playlists{id}")
  @Test
  void updatePlaylist() throws Exception {
    // Given
    PlaylistMother mother = new PlaylistMother(random).withAll();
    Playlist playlist = mother.get();
    Integer id = playlist.getId();

    String anotherValidName = mother.withNames().get().getName();
    PlaylistsPatchRequest request = new PlaylistsPatchRequest(Optional.of(anotherValidName));
    String payload = mapper.writeValueAsString(request);

    Playlist updatedPlaylist = request.patch(new Playlist(playlist));
    EntityMother.setId(updatedPlaylist, id);
    PlaylistResponse expectedResponse = new PlaylistResponse(updatedPlaylist);
    String expectedPayload = mapper.writeValueAsString(expectedResponse);

    // Stubbing
    when(playlists.find(id)).thenReturn(Optional.of(playlist));
    when(playlists.update(any(Playlist.class)))
        .thenAnswer((invocation -> invocation.getArgument(0)));

    // When
    mvc.perform(
            patch("/api/v2/playlists/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        // Then
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(expectedPayload));

    verify(playlists, times(1)).update(updatedPlaylist);
  }

  /**
   * @see PlaylistsController#deletePlaylist(int)
   */
  @DisplayName("DELETE /playlists/{id}")
  @Test
  void deletePlaylist() throws Exception {
    // Given
    PlaylistMother mother = new PlaylistMother(random).withIds();
    Playlist playlist = mother.get();
    Integer id = playlist.getId();

    // Stubbing
    when(playlists.find(id)).thenReturn(Optional.of(playlist));

    // When
    mvc.perform(delete("/api/v2/playlists/{id}", id))
        // Then
        .andExpect(status().isNoContent())
        .andExpect(header().doesNotExist("content-type"));

    verify(playlists, times(1)).delete(playlist);
  }

  /**
   * @return stream of all {@code requests} that target a specific {@code Playlist} resource.
   * @see #playlistNotFound
   */
  private Stream<TestSourceRequest> playlistRequests() {
    // Given
    final int id = random.nextInt(100);

    Collection<TestSourceRequest> requests = new ArrayList<>(3);
    requests.add(new TestSourceRequest("GET /playlists/{id}", get("/api/v2/playlists/{id}", id)));
    requests.add(
        new TestSourceRequest("DELETE /playlists/{id}", delete("/api/v2/playlists/{id}", id)));

    String validPlaylistName = new PlaylistMother(random).withNames().get().getName();
    PlaylistsPatchRequest patchRequest = new PlaylistsPatchRequest(Optional.of(validPlaylistName));
    String patchPayload = mapper.writeValueAsString(patchRequest);
    var patch =
        patch("/api/v2/playlists/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(patchPayload);
    requests.add(new TestSourceRequest("PATCH /playlists/{id}", patch));

    return requests.stream();
  }

  @DisplayName("404 Not Found Routes")
  @MethodSource("playlistRequests")
  @ParameterizedTest
  void playlistNotFound(TestSourceRequest request) throws Exception {
    // Stubbing
    when(playlists.find(anyInt())).thenReturn(Optional.empty());

    // When
    mvc.perform(request.requestBuilder())
        // Then
        .andExpect(status().isNotFound());
  }

  /**
   * A {@link MockHttpServletRequestBuilder} wrapped in a {@code record} for use as a {@link
   * MethodSource} return type.
   *
   * @param displayName if we want a better test harness, to see which routes failed the validation,
   *     we do sorta need this.
   * @param requestBuilder for the test invoke {@link MockMvc#perform(RequestBuilder)} on.
   */
  record TestSourceRequest(String displayName, MockHttpServletRequestBuilder requestBuilder) {

    @NonNull
    @Override
    public String toString() {
      return displayName;
    }
  }
}
