package com.bobo.semantic;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * A configurable, chainable {@link Supplier} of a type of {@code Object}.
 *
 * <p>A {@code Mother} is knowledgeable about the needs (invariants) of her children, and will
 * produce well-formed objects for the purpose of testing, unless otherwise instructed.
 *
 * <p>A {@code Mother} is allowed to over-step the boundaries of her children; she may be asked to
 * expose {@code protected} members of a child, but is respectful enough to never expose {@code
 * private} members.
 *
 * <p>A {@code Mother} is not guaranteed to be immutable; instances should not be reused between
 * tests.
 *
 * @param <T> the type of the {@code Mother}'s child.
 */
public interface Mother<T> extends Supplier<T> {

  /**
   * Convenience method to supply {@code n} children with the {@code Mother}'s current
   * configuration.
   *
   * @param n number of children to create.
   * @return stream of children.
   * @see Stream#toList()
   * @see Collectors#toSet()
   */
  default Stream<T> get(int n) {
    return IntStream.range(1, n + 1).mapToObj((i) -> this.get());
  }

  /**
   * Configure the {@code Mother} to create children with <b>all</b> their fields populated with
   * mock data.
   *
   * <p>This is always equivalent to chaining all the {@code Mother}'s {@code with...()} methods.
   * This means all <b>previous</b> configurations would be overridden.
   *
   * <p>A {@code Mother} is primarily a unit-testing utility. Prefer configuring the {@code Mother}
   * explicitly using her {@code with...(...)} methods, to be more declarative about what you are
   * testing.
   */
  Mother<T> withAll();
}
