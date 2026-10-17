package com.bobo.storage.web.api.tag;

import com.bobo.storage.web.semantic.ResourceAPI;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Tags")
@RequestMapping("/tags")
interface TagAPI extends ResourceAPI {

  @PostMapping
  @ApiResponse(responseCode = "201")
  ResponseEntity<@Valid TagResponse> createTag(@Valid @RequestBody TagCreateRequest request);
}
