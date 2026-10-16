package com.bobo.storage.web.semantic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Defines a schema for an API request. Either the request body schema, or its supported URI query
 * parameters.
 *
 * @implSpec Concrete implementations should be {@code package-private} to their {@link
 *     ResourceAPI}, and use the suffix {@code ...Request} or {@code ...Query}.
 * @see RequestBody
 * @see ModelAttribute
 * @see Valid
 * @see Schema
 */
public interface RequestSchema {}
