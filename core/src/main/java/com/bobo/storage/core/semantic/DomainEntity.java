package com.bobo.storage.core.semantic;

import static com.bobo.storage.core.semantic.AccessForTesting.Modifier;

import com.bobo.semantic.TechnicalID;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

/**
 * Represents an entity within the domain model.
 *
 * <p>{@code DomainEntities} are expected to enforce their domain invariants and expose meaningful
 * behaviour, ensuring the integrity and consistency of the domain model.
 *
 * <p>Fields should be declared in order of importance to the concept being modelled.
 */
@MappedSuperclass
public abstract class DomainEntity implements TechnicalID<Integer> {

  /**
   * Technical identifier of this entity.
   *
   * <p>See JSR 338: Java Persistence API (JPA) Specification (v2.2), section 2.3.1 "Default Access
   * Type":
   *
   * <blockquote>
   *
   * The default access type of an entity hierarchy is determined by the placement of mapping
   * annotations on the attributes of the entity classes.
   *
   * </blockquote>
   *
   * <p>By placing the {@link Id} annotation on this field, all implementing entities inherit
   * field-based access ({@link AccessType#FIELD}). This enforces a consistent access strategy
   * across the hierarchy, as mixing field and property access is not allowed.
   *
   * <p>Alternatively, access could be explicitly configured using {@link Access}.
   *
   * @see TechnicalID
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  /**
   * Base constructor for {@code DomainEntity}.
   *
   * <p>See JPA 2.2, section 2.1 "The Entity Class":
   *
   * <blockquote>
   *
   * The entity class must have a no-arg constructor. The entity class may have other constructors
   * as well. The no-arg constructor must be public or protected.
   *
   * </blockquote>
   *
   * <p>A {@code protected} no-args constructor is required for all {@code DomainEntity}
   * implementations.
   *
   * <p>All {@code DomainEntity} instances must be valid upon construction via a {@code public}
   * constructor, and are responsible for maintaining their invariants during mutation.
   */
  protected DomainEntity() {}

  /**
   * The {@link TechnicalID} of a {@link DomainEntity} is managed exclusively by the persistence
   * provider.
   *
   * @param id the technical identifier to assign to this {@link Entity}
   * @see TechnicalID#getId()
   * @implNote Should be marked {@code final}, but the JPA provider (Hibernate) proxies the mutators
   *     for lazy-loading operations.
   */
  @AccessForTesting(Modifier.PACKAGE_PRIVATE)
  void setId(Integer id) {
    this.id = id;
  }

  /**
   * @implNote Should be marked {@code final}, but the JPA provider (Hibernate) proxies the
   *     accessors for lazy-loading operations.
   */
  @Override
  public Integer getId() {
    return id;
  }

  /**
   * e.g. {@code Entity(id:1)}
   *
   * <p>This method exists to keep logs consistent yet concise. When logging, less is often more—but
   * context is essential.
   *
   * <p>If a {@code DomainEntity} appears in a log message, we need to be able to identify it within
   * the system for further investigation. This is one of the primary use cases for the {@code
   * TechnicalID}.
   *
   * <h2>Implementation Notes</h2>
   *
   * <p>A transient (unmanaged) entity cannot be traced by its {@code id} and is represented as
   * {@code Entity(<new>)} as a fallback. In such cases, prefer logging identifying fields
   * explicitly, and avoid relying solely on this method.
   *
   * @return the minimal information needed to identify this entity in logs.
   */
  public final String log() {
    String ref = (id != null) ? "(id:" + id + ")" : "(<new>)";
    return this.getClass().getSimpleName() + ref;
  }

  /**
   * e.g. {@code 2 Entity(ids:1,2)}
   *
   * @param entities a non-empty, homogeneous collection of entities to log information about.
   * @return the minimal information needed to identify these entities in logs.
   * @throws IllegalArgumentException if the collection is empty, or the collection is
   *     non-homogenous.
   * @implNote Technically, we could support non-homogeneous collection by producing {@code n
   *     EntityA(ids:1, 2), n EntityB(ids:3, 4} but I do not see a use case for this. It would be
   *     very improbable to have a collection of several different entity types, and attempt to log
   *     them all together in a single message.
   * @see #log()
   */
  public static String log(Collection<? extends DomainEntity> entities)
      throws IllegalArgumentException {
    if (Objects.requireNonNull(entities).isEmpty()) {
      throw new IllegalArgumentException(
          """
          Attempt to log information about an empty collection. \
          Caller is missing a guard clause to avoid unnecessary executions.\
          """);
    }

    Class<?> entityClass = null;
    StringBuilder refs = new StringBuilder(entities.size() * 4);
    for (Iterator<? extends DomainEntity> iterator = entities.iterator(); iterator.hasNext(); ) {
      DomainEntity entity = iterator.next();
      if (entityClass == null) {
        entityClass = entity.getClass();
      } else if (!entityClass.isInstance(entity)) {
        throw new IllegalArgumentException(
            """
            Attempt to log information about a non-homogeneous collection. \
            Cannot create a single, identifiable logging representation for multiple entity types at once.\
            """);
      }
      String entityRef = entity.getId() != null ? entity.getId().toString() : "<new>";
      refs.append(entityRef);
      if (iterator.hasNext()) {
        refs.append(", ");
      }
    }
    assert entityClass
        != null; // Loop will execute at-least once as we have validated it is not empty.
    return "%d %s(ids:%s)".formatted(entities.size(), entityClass.getSimpleName(), refs);
  }
}
