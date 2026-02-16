package com.bobo.storage.core.playlist.song;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.semantic.EntityRepository;
import com.bobo.storage.core.song.Song;
import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @implSpec {@link EntityRepository}
 */
@Repository
interface PlaylistSongRepository
    extends EntityRepository<PlaylistSong, Integer>, CrudRepository<PlaylistSong, Integer> {

  Collection<PlaylistSong> findAllByPlaylist(Playlist playlist);

  Collection<PlaylistSong> findAllBySong(Song song);
}
