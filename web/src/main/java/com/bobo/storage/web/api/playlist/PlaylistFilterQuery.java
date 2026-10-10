package com.bobo.storage.web.api.playlist;

import com.bobo.storage.core.playlist.Playlist;
import com.bobo.storage.web.semantic.FilterQuery;
import java.util.Optional;

/**
 * @param title an optional title fragment used as a case-insensitive search filter. If specified,
 *     must not be blank (see {@link String#isBlank()}).
 */
public record PlaylistFilterQuery(Optional<String> title) implements FilterQuery<Playlist> {}
