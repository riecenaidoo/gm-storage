package com.bobo.storage.core.semantic;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Small, deterministic transformations applied to a value.
 *
 * <p>Intended to clean and standardise data before persistence, comparison, or further processing.
 */
public class Normalisations {

  /**
   * Returns a {@link Function} that conditionally transforms its input to {@code null}.
   *
   * @param predicate the condition under which the input to the {@link Function} should be replaced
   *     with {@code null}, e.g. {@link String#isBlank()}.
   * @param <T> the type of the input being tested.
   * @return a {@link Function} that returns {@code null} if the predicate evaluates to {@code
   *     true}, otherwise returns the unchanged input.
   * @apiNote the {@link Function} is null-safe. It will return {@code null} for {@code null}
   *     inputs.
   */
  public static <T> Function<T, T> nullIf(Predicate<T> predicate) {
    Objects.requireNonNull(predicate);
    return argument -> (argument == null || predicate.test(argument)) ? null : argument;
  }

  /**
   * Returns a {@link Function} composed with a {@code null} guard.
   *
   * @param function the {@link Function} to compose.
   * @return a {@link Function} that skips execution if the input is {@code null}, otherwise applies
   *     the composed {@code function}.
   * @see Function#compose(Function)
   */
  public static <T, R> Function<T, R> skipIfNull(Function<T, R> function) {
    Objects.requireNonNull(function);
    return argument -> (argument == null) ? null : function.apply(argument);
  }

  /**
   * Returns a {@link Function} that truncates a {@link String} to size, if it exceeds a character
   * limit.
   *
   * @param limit the maximum number of characters allowed.
   * @return a {@link Function} that truncates (substrings) its input to the specified {@code limit}
   *     when the input length exceeds that limit; otherwise, it is a no-op and returns the input
   *     unchanged.
   * @throws IllegalArgumentException if the {@code limit} is less than or equal to zero.
   * @apiNote The {@link Function} is not null-safe. The caller should be explicit about requiring
   *     null-safety and compose with {@link #skipIfNull(Function)}, if required.
   */
  public static Function<String, String> truncateToSize(int limit) {
    if (limit <= 0)
      throw new IllegalArgumentException(
          (limit == 0)
              ? "Truncation to zero size non-nonsensical. Did you mean to set the String to blank?"
              : "Cannot truncate to negative size of '%d'.".formatted(limit));
    return argument -> (argument.length() > limit) ? argument.substring(0, limit) : argument;
  }
}
