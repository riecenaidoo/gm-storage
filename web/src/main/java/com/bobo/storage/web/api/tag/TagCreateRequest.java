package com.bobo.storage.web.api.tag;

import com.bobo.storage.core.tag.Tag;
import com.bobo.storage.web.semantic.CreateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record TagCreateRequest(@NotNull @NotBlank String tag) implements CreateRequest<Tag> {
  @Override
  public Tag toCreate() {
    return new Tag(tag);
  }
}
