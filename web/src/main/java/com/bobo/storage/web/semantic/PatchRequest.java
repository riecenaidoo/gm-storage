package com.bobo.storage.web.semantic;

import java.util.function.Consumer;
import org.springframework.http.HttpMethod;

/**
 * A request to apply one or more mutations to (the state of) a {@code Resource}.
 *
 * @param <T> the type of {@code Resource} this {@code PATCH} operates on.
 * @see Consumer
 * @see HttpMethod#PATCH
 */
public interface PatchRequest<T> {

  /**
   * Applies the {@code PATCH} to a {@code Resource}.
   *
   * @param resource to apply the {@code PATCH} to. Mutates.
   * @return same {@code Object} for fluency.
   */
  T patch(T resource);
}
