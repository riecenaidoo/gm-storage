package com.bobo.storage.web.api.v2.controller;

import com.bobo.semantic.TechnicalID;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A {@code Resource} could not be found.
 *
 * @see Optional#orElseThrow(Supplier)
 */
public class ResourceNotFoundException extends RuntimeException {

  private final String resourceName;

  private final String identifier;

  public <T extends TechnicalID<ID>, ID> ResourceNotFoundException(Class<T> resource, ID id) {
    this.resourceName =
        resource.getSimpleName(); // TODO [design] Resource#getName() - allow a Resource to override
    // this.
    this.identifier = id.toString();
  }

  public String getResourceName() {
    return resourceName;
  }

  public String getIdentifier() {
    return identifier;
  }
}
