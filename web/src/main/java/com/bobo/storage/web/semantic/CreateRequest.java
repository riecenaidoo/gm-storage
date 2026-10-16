package com.bobo.storage.web.semantic;

/**
 * A {@code POST} request that creates a resource.
 *
 * @param <T> the type resource this request produces; its creation.
 */
public interface CreateRequest<T> extends RequestSchema {

  /**
   * Construct a resource from the request payload.
   *
   * @apiNote The implementation is expected to be a simple delegation to a constructor or factory
   *     method. It takes no args, because the request should contain all the necessary information
   *     to perform the construction. If additional information is needed, or the construction is
   *     not straightforward, then use a basic {@link RequestSchema} and perform construction in a
   *     {@link ResourceController}.
   * @return a valid, but transient resource.
   */
  T toCreate();
}
