package com.bobo.semantic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;

/**
 * A test that verifies the expected behaviour of an isolated "unit" of code.
 *
 * <p>Typically at tests the contract of {@code public} methods. The contract is what has been
 * defined by the method signature, and the words in its documentation.
 *
 * @implSpec Suffix test classes with {@code Test}, which is the default pattern the Maven Surefire
 *     plugin uses to detect unit tests for the test cycle ({@code mvn test}).
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag(UnitTest.TAG)
public @interface UnitTest {

  /**
   * @return the class whose behaviour is being verified by this {@link UnitTest}.
   */
  Class<?> value() default Object.class;

  /**
   * The {@link Tag} value to use for {@link UnitTest}.
   *
   * <p>Tagging via JUnit enables running a slice of tests via Maven.
   *
   * <pre>{@code
   * mvn test -Dgroups="unit"
   * }</pre>
   *
   * @see Tags
   * @apiNote Annotating a test class with {@link UnitTest} will automatically apply this tag. This
   *     {@code TAG} constant is available for special cases where you may want to refer to it
   *     directly.
   */
  String TAG = "unit";
}
