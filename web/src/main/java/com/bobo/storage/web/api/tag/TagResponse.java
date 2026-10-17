package com.bobo.storage.web.api.tag;

import com.bobo.storage.core.tag.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TagResponse(@NotNull Integer id, @NotNull @NotBlank String tag) {
  public TagResponse(Tag tag) {
    this(tag.getId(), tag.getTag());
  }
}
