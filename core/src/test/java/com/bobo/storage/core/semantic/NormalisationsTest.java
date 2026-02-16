package com.bobo.storage.core.semantic;

import static com.bobo.storage.core.semantic.Normalisations.nullIf;
import static com.bobo.storage.core.semantic.Normalisations.skipIfNull;
import static com.bobo.storage.core.semantic.Normalisations.truncateToSize;

import com.bobo.semantic.UnitTest;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@UnitTest(Normalisations.class)
class NormalisationsTest {

  /**
   * @see Normalisations#nullIf(Predicate)
   */
  @Nested
  class NullIf {

    /** A {@link Function} that returns {@code null} if the predicate evaluates to {@code true} */
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void predicateTrue(String aBlankString) {
      // Given
      Function<String, String> nullIfBlank = nullIf(String::isBlank);
      // When
      Assertions.assertNull(nullIfBlank.apply(aBlankString));
    }

    /**
     * Otherwise returns the unchanged input
     *
     * @see #predicateTrue(String)
     */
    @ParameterizedTest
    @ValueSource(strings = {"a", "non\t", " blank\n", "Str ing "})
    void predicateFalse(String aNonBlankString) {
      // Given
      Function<String, String> nullIfBlank = nullIf(String::isBlank);
      // When
      Assertions.assertSame(
          aNonBlankString,
          nullIfBlank.apply(aNonBlankString),
          "return the i.e. \"unchanged\" input.");
    }

    /** The {@link Function} is null-safe. It will return {@code null} for {@code null} inputs. */
    @Test
    void nullSafe() {
      // Given
      Function<String, String> nullIfBlank = nullIf(String::isBlank);
      // When
      Assertions.assertNull(nullIfBlank.apply(null));
    }

    @Test
    void raiseNPE() {
      Assertions.assertThrows(
          NullPointerException.class,
          () -> nullIf(null),
          "Raise NPE early to prevent creating a Function that will always raise an NPE when"
              + " called.");
    }
  }

  /**
   * @see Normalisations#skipIfNull(Function)
   */
  @Nested
  class SkipIfNull {

    @Test
    void isNull() {
      // Given
      Function<?, ?> fn =
          (arg) ->
              // When
              Assertions.fail("Function should not be called.");
      // Then
      skipIfNull(fn).apply(null); // Should not fail.
    }

    @Test
    void notNull() {
      // Given
      Function<Boolean, Boolean> fn = (arg) -> true;
      // When
      Boolean output = skipIfNull(fn).apply(false);
      // Then
      Assertions.assertTrue(output);
    }

    @Test
    void raiseNPE() {
      Assertions.assertThrows(
          NullPointerException.class,
          () -> skipIfNull(null),
          "Raise NPE early to prevent creating a Function that will always raise an NPE when"
              + " called.");
    }
  }

  /**
   * @see Normalisations#truncateToSize(int)
   */
  @Nested
  class TruncateToSize {

    /**
     * A {@link Function} that truncates (substrings) its input to the specified {@code limit} when
     * the input length exceeds that limit.
     */
    @ParameterizedTest
    @ValueSource(strings = {"suddenly", "overwhelmed", "desperate"})
    void aboveLimit(String aboveLimit) {
      // Given
      int limit = 5;
      assert aboveLimit.length() > limit : "Test Assumption";
      Function<String, String> truncateToSize = truncateToSize(limit);
      // When
      Assertions.assertEquals(limit, truncateToSize.apply(aboveLimit).length());
    }

    /**
     * Otherwise, it is a no-op and returns the input unchanged.
     *
     * @see #aboveLimit(String)
     */
    @ParameterizedTest
    @ValueSource(strings = {"then", "were", "they"})
    void atLimit(String atLimit) {
      // Given
      int limit = 4;
      assert atLimit.length() == limit : "Test Assumption";
      Function<String, String> truncateToSize = truncateToSize(limit);
      // When
      Assertions.assertSame(atLimit, truncateToSize.apply(atLimit));
    }

    /**
     * @throws IllegalArgumentException if the {@code limit} is less than or equal to zero.
     */
    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void raiseInvalidLimit(int illegalLimit) {
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> truncateToSize(illegalLimit),
          "Raise IAE early to prevent creating a Function that will always raise an IAE when"
              + " called.");
    }

    /**
     * Otherwise, it is a no-op and returns the input unchanged.
     *
     * @see #aboveLimit(String)
     */
    @ParameterizedTest
    @ValueSource(strings = {"a", "quick", "brown", "fox", "jumped"})
    void belowLimit(String belowLimit) {
      // Given
      int limit = 10;
      assert belowLimit.length() < limit : "Test Assumption";
      Function<String, String> truncateToSize = truncateToSize(limit);
      // When
      Assertions.assertSame(belowLimit, truncateToSize.apply(belowLimit));
    }

    /** The {@link Function} is not null-safe. */
    @ParameterizedTest
    @ValueSource(ints = {1, 100, Integer.MAX_VALUE})
    void notNullSafeNull(int limit) {
      // When
      Assertions.assertThrows(NullPointerException.class, () -> truncateToSize(limit).apply(null));
    }
  }
}
