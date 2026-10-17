package com.bobo.storage.web.api.tag;

import com.bobo.storage.core.tag.Tag;
import com.bobo.storage.core.tag.TagService;
import com.bobo.storage.web.semantic.ResourceController;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ResourceController
class TagsController implements TagAPI {

  private final TagService tags;

  TagsController(TagService tags) {
    this.tags = tags;
  }

  @Override
  public ResponseEntity<@Valid TagResponse> createTag(TagCreateRequest request) {
    Tag tag = request.toCreate();
    tag = tags.add(tag);
    TagResponse response = new TagResponse(tag);
    URI resource =
        ServletUriComponentsBuilder.fromCurrentRequestUri()
            .pathSegment(String.valueOf(tag.getId()))
            .build()
            .toUri();
    return ResponseEntity.created(resource).body(response);
  }
}
