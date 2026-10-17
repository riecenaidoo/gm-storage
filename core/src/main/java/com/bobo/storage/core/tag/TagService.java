package com.bobo.storage.core.tag;

import com.bobo.storage.core.semantic.Create;
import com.bobo.storage.core.semantic.EntityService;
import com.bobo.storage.core.semantic.Read;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class TagService implements EntityService<Tag>, Create<Tag>, Read<Tag> {

  private final TagRepository tags;

  TagService(TagRepository tags) {
    this.tags = tags;
  }

  @Override
  public Tag add(Tag tag) throws IllegalArgumentException {
    if (Objects.nonNull(tag.getId())) throw new IllegalArgumentException();

    Optional<Tag> existingTag = tags.findByTag(tag.getTag());
    return existingTag.orElseGet(() -> tags.save(tag));
  }

  @Override
  public Optional<Tag> find(int id) {
    return tags.findById(id);
  }
}
