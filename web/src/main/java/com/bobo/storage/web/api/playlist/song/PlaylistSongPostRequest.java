package com.bobo.storage.web.api.playlist.song;

import jakarta.validation.constraints.NotNull;

record PlaylistSongPostRequest(@NotNull int songId) {}
