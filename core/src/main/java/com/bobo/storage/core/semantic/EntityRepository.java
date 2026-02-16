package com.bobo.storage.core.semantic;

import com.bobo.semantic.TechnicalID;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/**
 * Represents a collection of entities.
 *
 * <p>An {@link EntityRepository} controls access to persistent stores, and marks the resource layer
 * of {@link com.bobo.storage.core}.
 *
 * @apiNote Instances of this repository should be treated as collective nouns representing their
 *     entity type. e.g.
 *     <pre>
 *   {@code private final AppleRepository apples;}
 * </pre>
 *
 * @implSpec
 *     <ul>
 *       <li>The access modifier for an {@link EntityRepository} must be default (package-private).
 *           Only the {@link EntityService}, for the same {@link DomainEntity}, may declare a
 *           dependency on the {@link EntityRepository}.
 *       <li>Method definitions should be grouped by CRUD operation. Within each group, singular
 *           operations should be defined before batch operations. Method order and parameter lists
 *           should reflect the field order of the associated entity (see {@link DomainEntity}).
 *       <li>These classes may be aware of other layers or technical concerns, but must not depend
 *           on them.
 *     </ul>
 */
@NoRepositoryBean
public interface EntityRepository<T extends TechnicalID<ID>, ID> extends Repository<T, ID> {

  /**
   * Find a technically identified {@code Entity}.
   *
   * @param id {@link TechnicalID} of the {@code Entity}.
   * @return the {@code Entity}, if it exists, otherwise {@link Optional#empty()}.
   */
  Optional<T> findById(ID id);
}
