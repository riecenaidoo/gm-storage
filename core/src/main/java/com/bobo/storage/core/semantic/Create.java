package com.bobo.storage.core.semantic;

import com.bobo.semantic.TechnicalID;

/**
 * Capable of creating a resource.
 *
 * @param <T> the type of resource.
 */
public interface Create<T extends DomainEntity> {

  /**
   * Add a resource to the system. It will be assigned a {@link TechnicalID}.
   *
   * @param resource the resource to add; must not already have an assigned {@code id}.
   * @return a resource representing the one added to the system. Guaranteed to be equal to, but not
   *     necessarily the same object instance.
   * @throws IllegalArgumentException if the {@code resource} already has an assigned {@code id}.
   * @apiNote This is the only supported signature for creation operations. Resource construction is
   *     controlled by the resource itself, but addition to the system requires coordination. This
   *     ensures system-level invariants are enforced during addition.
   */
  T add(T resource) throws IllegalArgumentException;
}
