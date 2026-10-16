package com.bobo.storage.web.semantic;

import org.springframework.http.ResponseEntity;

/**
 * Define the schema for an API request response. This is usually a DTO for a resource.
 *
 * @apiNote This is an important boundary object. A resource can evolve over time; fields can be
 *     added, removed or names changed. A change to a resource should not automatically change the
 *     existing API contract, as this can break Client implementations, or expose information not
 *     intended to be serialized.
 * @implSpec Concrete implementations should use the suffix {@code ...Response}. A resource should
 *     ideally have a single, consistent schema across the API, so only one implementation is
 *     expected per resource. A "view" or "projection" of a resource should have its own separate
 *     {@link ResourceAPI} and {@link ResponseSchema}.
 * @see ResponseEntity
 */
public interface ResponseSchema {}
