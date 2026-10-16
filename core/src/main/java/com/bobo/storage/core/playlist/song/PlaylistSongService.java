package com.bobo.storage.core.playlist.song;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.semantic.CoreService;
import com.bobo.storage.core.semantic.Create;
import com.bobo.storage.core.semantic.EntityService;
import com.bobo.storage.core.semantic.Read;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@CoreService
public class PlaylistSongService
    implements EntityService<PlaylistSong>, Create<PlaylistSong>, Read<PlaylistSong> {

  private final PlaylistSongRepository playlistSongs;

  PlaylistSongService(PlaylistSongRepository playlistSongs) {
    this.playlistSongs = playlistSongs;
  }

  @Override
  @Transactional
  public PlaylistSong add(PlaylistSong playlistSong) {
    if (Objects.nonNull(playlistSong.getId())) throw new IllegalArgumentException();
    return playlistSongs.save(playlistSong);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<PlaylistSong> find(int id) {
    return playlistSongs.findById(id);
  }

  /**
   * @param playlist the source {@link Playlist}.
   * @return all {@link PlaylistSong} resources within the given {@link Playlist}; never {@code
   *     null}.
   * @implSpec {@link Read#get()}
   */
  @Transactional(readOnly = true)
  public Collection<PlaylistSong> getFromPlaylist(Playlist playlist) {
    return playlistSongs.findAllByPlaylist(playlist);
  }

  @Transactional
  public void delete(PlaylistSong song) {
    playlistSongs.delete(song);
  }
}
