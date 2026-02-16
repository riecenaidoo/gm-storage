package com.bobo.storage.web.api.v2.controller;

import com.bobo.semantic.TechnicalID;

/**
 * Thrown when a {@code Resource} is assumed to be a {@code Subresource} of another, but no such
 * relationship exists.
 *
 * <p>Both resources exist, but they are not related as expected; the assumption about their
 * relationship is incorrect.
 */
public class SubresourceMismatchException extends RuntimeException {

  private final String resourceName;

  private final String identifier;

  private final String subResourceName;

  private final String subResourceIdentifier;

  public SubresourceMismatchException(TechnicalID<?> resource, TechnicalID<?> subResource) {
    this.resourceName = resource.getClass().getSimpleName();
    this.identifier = resource.getId().toString();
    this.subResourceName = subResource.getClass().getSimpleName();
    this.subResourceIdentifier = subResource.getId().toString();
  }

  public String getResourceName() {
    return resourceName;
  }

  public String getIdentifier() {
    return identifier;
  }

  public String getSubResourceName() {
    return subResourceName;
  }

  public String getSubResourceIdentifier() {
    return subResourceIdentifier;
  }
}
