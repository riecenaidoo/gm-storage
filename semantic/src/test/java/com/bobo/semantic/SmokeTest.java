package com.bobo.semantic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;

/**
 * A quick test that can be run to confirm the basic stability of the system.
 *
 * <p>Typically run after a build ({@code mvn compile}) to fail fast before running more exhaustive
 * tests.
 *
 * <p>Examples include confirming the application can boot up successfully, or the application
 * context can load without issues in a dependency-injected environment.
 *
 * @implSpec Suffix test class names with {@code Test}, because they should be run during regular
 *     test cycles ({@code mvn test}), but note that they are not considered {@link UnitTest}.
 *     Furthermore, although these tests may involve several systems, they are not considered {@link
 *     IntegrationTest}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag(SmokeTest.TAG)
public @interface SmokeTest {

  /**
   * The {@link Tag} value to use for {@link SmokeTest}.
   *
   * <p>Tagging via JUnit enables running a slice of tests via Maven.
   *
   * <pre>{@code
   * mvn test -Dgroups="smoke"
   * }</pre>
   *
   * @see Tags
   * @apiNote Annotating a test class with {@link SmokeTest} will automatically apply this tag. This
   *     {@code TAG} constant is available for special cases where you may want to refer to it
   *     directly.
   */
  String TAG = "smoke";
}
