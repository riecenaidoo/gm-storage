package com.bobo.storage.core.playlist.song;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.core.semantic.CoreService;
import com.bobo.storage.core.semantic.Create;
import com.bobo.storage.core.semantic.DomainEntity;
import com.bobo.storage.core.semantic.EntityService;
import com.bobo.storage.core.semantic.Read;
import com.bobo.storage.core.song.Song;
import com.bobo.storage.core.song.SongService;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@CoreService
public class PlaylistSongService
		implements EntityService<PlaylistSong>, Create<PlaylistSong>, Read<PlaylistSong> {

	private static final Logger log = LoggerFactory.getLogger(PlaylistSongService.class);

	private final PlaylistSongRepository playlistSongs;

	private final SongService songs;

	PlaylistSongService(PlaylistSongRepository playlistSongs, SongService songs) {
		this.playlistSongs = playlistSongs;
		this.songs = songs;
	}

  /**
   * @implNote This is not a great implementation. Perhaps it would be better to enforce PlaylistSong
   * creation via an existing Song and Playlist.
   */
	@Override
	@Transactional
	public PlaylistSong add(PlaylistSong playlistSong) {
		if (Objects.nonNull(playlistSong.getId())) throw new IllegalArgumentException();
		if (playlistSong.getSong().getId() == null) {
      Song song = playlistSong.getSong();
			song = songs.add(song);
      playlistSong.setSong(song);
		}
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

	/**
	 * Migrate the {@code Song} associated with a {@code PlaylistSong}.
	 *
	 * <p>This is typically done to facilitate merging {@code Songs} that have been discovered as
	 * identical, or when the original {@code Provider} can no longer host the {@code Song}, but
	 * another {@code Provider} does.
	 *
	 * @param from {@code Song} to transfer from.
	 * @param to {@code Song} to transfer to.
	 */
	@Transactional
	public void migrate(Song from, Song to) {
		Collection<PlaylistSong> songsToTransfer = playlistSongs.findAllBySong(from);
		songsToTransfer.forEach(song -> song.migrate(to));
		playlistSongs.saveAll(songsToTransfer);
		log.info(
				"PlaylistSong#Migration: {} migrated from {} to {}.",
				DomainEntity.log(songsToTransfer),
				from.log(),
				to.log());
	}

	@Transactional
	public void delete(PlaylistSong song) {
		playlistSongs.delete(song);
	}
}
