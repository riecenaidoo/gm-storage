package com.bobo.storage.core.semantic;

import java.util.Collection;
import java.util.Optional;

/**
 * Capable of reading a resource.
 *
 * @param <T> the type of resource.
 */
public interface Read<T extends DomainEntity> {

  /**
   * Find a resource by its technical identifier.
   *
   * @param id the technical identifier of the resource.
   * @return the resource if found, otherwise {@link Optional#empty()}.
   * @apiNote Further singular read methods should follow this return contract and use the {@code
   *     find} or {@code search} naming conventions.
   */
  Optional<T> find(int id);

  /**
   * Retrieve all available resources.
   *
   * @return a {@link Collection} of all available resources; never {@code null}.
   * @apiNote Further batch read methods should follow this return contract and use the {@code get}
   *     or {@code search} naming conventions.
   * @implNote This method exists primarily as a linkable, living specification for batch reads. In
   *     practice, it is rare to require an unfiltered read of all resources.
   */
  default Collection<T> get() {
    throw new UnsupportedOperationException(
        "Unfiltered batch read not supported. Implement get() if needed.");
  }
}
