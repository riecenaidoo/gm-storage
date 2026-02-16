package com.bobo.semantic;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Serves as an example pattern for laying out tests. */
public interface TestLayout {

  // --- Test Targets ---

  // Test Utilities

  // Mock Dependencies

  // --- Constructor ---

  // --- Test Setup ---

}

@SuppressWarnings("unused")
class ExampleTarget {

  public void method() {
    // ...
  }
}

/** Rationale for why the test classes are laid out the way they are. */
@SuppressWarnings("ALL")
@UnitTest(ExampleTarget.class)
class ExampleUnitTest {

  private ExampleTarget target;

  /**
   * When writing a single test for a method, it typically covers the "happy path" — confirming that
   * the method behaves correctly under normal conditions. In such cases, name the test after the
   * method being tested.
   *
   * <p>Use the {@code @see} tag to backlink to the method under test. This provides:
   *
   * <ul>
   *   <li>Quick navigation to the method via IDE features like "Go to definition".
   *   <li>Improved discoverability of test coverage when using "Find Usages" or similar tools.
   *   <li>A consistent convention that helps others locate relevant test cases faster.
   * </ul>
   *
   * @see ExampleTarget#method()
   */
  @Test
  void method() {
    // ...
  }

  // OR

  /**
   * Use a {@link Nested} class to group test cases.
   *
   * <p>As the number of test cases grows, grouping them becomes essential for readability and
   * maintenance. When no higher-level grouping exists (e.g., by scenario or behavior), default to
   * grouping by the method under test.
   *
   * <p>A {@link Nested} class is an execution point. It can be used as a harness to quickly run a
   * subset of test cases within a test class. If done at a method level, you can selectively
   * execute test harnesses for methods you are working on, and with a backlink ({@code see}) you
   * can quickly locate the harness if it exists. If using a {@link Nested} class, backlink the
   * class, not the many methods within.
   *
   * <p>A method-level test harness can supplement documentation and/or serve as a living
   * specification of the expected method behaviour.
   *
   * @see ExampleTarget#method()
   */
  @Nested
  class Method {

    void method() {
      // ...
    }

    void negativePath() {
      // ...
    }
  }
}
