package com.bobo.semantic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;

/**
 * A test that verifies the interaction between one or more components.
 *
 * <p>Typically, to verify the interaction between our components and third-party libraries or
 * systems. They can also be used to test assumptions about third-party components, and fail if
 * those assumptions our system relies on fail.
 *
 * <p>An {@link IntegrationTest} can also be used to verify our components work together correctly,
 * but as far as possible prefer defining strong contracts for our components and {@link UnitTest}
 * expected behaviour against those contracts.
 *
 * <p>An {@link IntegrationTest} may still mock or stub out components as needed, and this is
 * encouraged to narrow the scope of what is under test.
 *
 * <p>The nature of these tests makes them slower, as they may need to spin up the application
 * context, or a portion thereof. They are expected to be run before merging or releasing to confirm
 * we have not regressed, and should be run anytime dependencies are updated or changed.
 *
 * @implSpec Suffix test classes with {@code IT}, which is the default pattern the Maven Failsafe
 *     plugin uses to detect integration tests for the verification cycle ({@code mvn verify}).
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Tag(IntegrationTest.TAG)
public @interface IntegrationTest {

  /**
   * @return the component classes being tested.
   */
  Class<?>[] value() default {};

  /**
   * The {@link Tag} value to use for {@link IntegrationTest}.
   *
   * <p>Tagging via JUnit enables running a slice of tests via Maven.
   *
   * <pre>{@code
   * mvn verify -Dgroups="integration"
   * }</pre>
   *
   * @see Tags
   * @apiNote Annotating a test class with {@link IntegrationTest} will automatically apply this
   *     tag. This {@code TAG} constant is available for special cases where you may want to refer
   *     to it directly.
   */
  String TAG = "integration";

  /**
   * The {@link Tag} value to use for {@link IntegrationTest} tests that interact with external
   * systems.
   *
   * <p>We make a distinct because we cannot guarantee the status, or behaviour, of a system we do
   * not control, which makes these tests potentially "flaky".
   *
   * <p>These are tests we might run as part of a health check, but not in a CI/CD pipeline, or
   * local workflow.
   *
   * <p>Tagging via JUnit enables running a slice of tests via Maven.
   *
   * <pre>{@code
   * mvn verify -Dgroups="external"
   * }</pre>
   *
   * <p>Tagging also enables the exclusion of specific tests. Exclusion takes precedence; if a test
   * is both included and excluded via tags (tests can have multiple tags), it will be excluded.
   *
   * <pre>{@code
   * mvn verify -Dgroups="integration" -DexcludedGroups="external"
   * }</pre>
   *
   * @see Tags
   */
  String EXTERNAL_TAG = "external";
}
