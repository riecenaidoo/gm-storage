package com.bobo.semantic;

/**
 * Has a technical identifier.
 *
 * <p>It is used to uniquely identify an object in the system, and should not be used to uniquely
 * identify an object within the domain.
 *
 * <p>An object will define its own uniqueness, if that is required.
 *
 * <p>An object that needs to be persisted, or requires a URI should implement this interface.
 *
 * @param <T> type of the identifier.
 */
public interface TechnicalID<T> {

  /**
   * The technical identity of an object is assigned by the system. It should not have a publicly
   * accessible mutator, nor is it a parameter in any public constructor.
   *
   * @return the technical identity of this {@code Object}.
   */
  T getId();

  /**
   * Null-safe technical identity comparison.
   *
   * <h2>Implementation Note</h2>
   *
   * <p>The in-line assignment is purely to flex on you.
   *
   * @param target commutative, nullable.
   * @param other commutative, nullable.
   * @return {@code true} if the objects are non-null, of the same {@code class}, and have the
   *     <strong>same</strong> technical identity.
   */
  static <T> boolean same(TechnicalID<T> target, TechnicalID<T> other) {
    Object targetId;
    return target != null
        && other != null
        && target.getClass().equals(other.getClass())
        && (targetId = target.getId()) != null
        && targetId.equals(other.getId());
  }
}
