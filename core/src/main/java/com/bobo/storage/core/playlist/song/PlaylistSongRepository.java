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

  /**
   * @return {@link PlaylistSong}(s) linked to the {@link Playlist}.
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  Collection<PlaylistSong> findAllByPlaylist(Playlist playlist);

  /**
   * @return {@link PlaylistSong}(s) linked to the {@link Song}.
   * @apiNote The {@link Collection} has no guarantees on order or uniqueness.
   */
  Collection<PlaylistSong> findAllBySong(Song song);
}
