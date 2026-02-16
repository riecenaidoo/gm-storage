package com.bobo.storage.core.semantic;

import com.bobo.semantic.Mother;
import java.util.function.Supplier;

/**
 * A {@code Mother} of a {@link DomainEntity}.
 *
 * @see Mother
 */
public interface EntityMother<T extends DomainEntity> extends Mother<T> {

  EntityMother<T> withIds(Supplier<Integer> idSupplier);

  EntityMother<T> withIds();

  /**
   * Assign an {@code id} to the {@code Entity}.
   *
   * <p>The signature to be used when the mocking is not concerned with what the {@code id} is, just
   * that it exists.
   */
  T setId(T entity);

  /**
   * Assign an {@code id} to the {@code Entity}.
   *
   * <p>To signature to be used when you need control over the mocked {@code id}.
   */
  static <T extends DomainEntity> T setId(T entity, Integer id) {
    entity.setId(id);
    return entity;
  }
}
