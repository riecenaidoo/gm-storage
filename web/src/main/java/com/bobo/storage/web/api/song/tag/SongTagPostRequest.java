package com.bobo.storage.web.api.song.tag;

import com.bobo.storage.web.semantic.RequestSchema;
import jakarta.validation.constraints.NotNull;

record SongTagPostRequest(@NotNull int tagId) implements RequestSchema {}
