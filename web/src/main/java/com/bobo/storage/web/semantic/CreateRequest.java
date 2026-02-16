package com.bobo.storage.web.semantic;

/**
 * A {@code POST Request} that creates a resource.
 *
 * @param <C> the type resource this {@code Request} produces; its creation.
 */
public interface CreateRequest<C> {

  /**
   * TODO [design] possibly throw 422 Unprocessable Content if the construction of the resource
   * fails.
   *
   * @return a representation of the desired resource this request wishes to create.
   */
  C toCreate();
}
