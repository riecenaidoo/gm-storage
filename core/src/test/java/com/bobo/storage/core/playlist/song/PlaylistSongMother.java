package com.bobo.storage.core.playlist.song;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.playlist.PlaylistMother;
import com.bobo.storage.core.semantic.EntityMother;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongMother;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

public class PlaylistSongMother implements EntityMother<PlaylistSong> {

  private final Random random;

  private Supplier<Integer> ids;

  private Supplier<Playlist> playlists;

  private Supplier<Song> songs;

  public PlaylistSongMother(Random random) {
    this.random = random;
  }

  /**
   * While the default constructor is always provided, prefer {@link
   * PlaylistSongMother#PlaylistSongMother(Random)} where possible.
   */
  @SuppressWarnings("unused")
  public PlaylistSongMother() {
    this(new Random());
  }

  @Override
  public PlaylistSong get() {
    PlaylistSong playlistSong = new PlaylistSong();

    Integer id = Objects.isNull(ids) ? null : ids.get();
    Playlist playlist =
        Objects.isNull(playlists)
            ? (playlists = new PlaylistMother(random).withIds()).get()
            : playlists.get();
    Song song =
        Objects.isNull(songs) ? (songs = new SongMother(random).withIds()).get() : songs.get();

    EntityMother.setId(playlistSong, id);
    playlistSong.setPlaylist(playlist);
    playlistSong.setSong(song);

    return playlistSong;
  }

  @Override
  public PlaylistSongMother withAll() {
    return this.withIds();
  }

  @Override
  public PlaylistSong setId(PlaylistSong playlistSong) {
    return EntityMother.setId(playlistSong, random.nextInt());
  }

  @Override
  public PlaylistSongMother withIds(Supplier<Integer> ids) {
    this.ids = ids;
    return this;
  }

  @Override
  public PlaylistSongMother withIds() {
    return withIds(this.random::nextInt);
  }

  public PlaylistSongMother withPlaylists(Supplier<Playlist> playlists) {
    this.playlists = playlists;
    return this;
  }

  public PlaylistSongMother withPlaylists() {
    PlaylistMother playlists = new PlaylistMother(random);
    return withPlaylists(playlists);
  }

  public PlaylistSongMother withSongs(Supplier<Song> songs) {
    this.songs = songs;
    return this;
  }

  public PlaylistSongMother withSongs() {
    SongMother songs = new SongMother(random);
    return withSongs(songs);
  }
}
