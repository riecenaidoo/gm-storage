package com.bobo.storage.web.api.playlist.song;

import com.bobo.storage.web.semantic.RequestSchema;
import jakarta.validation.constraints.NotNull;

record PlaylistSongPostRequest(@NotNull int songId) implements RequestSchema {}
