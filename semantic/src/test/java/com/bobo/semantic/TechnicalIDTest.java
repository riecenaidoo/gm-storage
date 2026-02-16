package com.bobo.semantic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@UnitTest(TechnicalID.class)
class TechnicalIDTest implements TestLayout {

  /**
   * Use a {@link Nested} class with a {@code see} to backlink to the method being unit-tested.
   *
   * <p>As test cases grow, grouping them becomes important for maintenance. If test cases have no
   * logical grouping, default to grouping by method. It serves as a supplement to the
   * documentation, and provides a "harness" to run all unit-tests for that method.
   *
   * <p>Can also be used to add further documentation. For example, writing these test cases was
   * tedious but simple and quite quick. As this utility method is expected to be used frequently,
   * the trade-off was worth.
   *
   * @see TechnicalID#same(TechnicalID, TechnicalID)
   */
  @Nested
  class Same {

    /**
     * We can use generics to ensure they have the same technical identifying type, but we must
     * still validate that they are the same class.
     */
    @Test
    void differentClasses() {
      final int id = 1;
      TechnicalID<Integer> target = new TechnicalIDImpl(id);
      TechnicalID<Integer> other = () -> id;
      Assertions.assertFalse(TechnicalID.same(target, other));
    }

    @Test
    void targetNull() {
      Assertions.assertFalse(TechnicalID.same(null, new TechnicalIDImpl(1)));
    }

    @Test
    void otherNull() {
      Assertions.assertFalse(TechnicalID.same(new TechnicalIDImpl(1), null));
    }

    @Test
    void targetIdNull() {
      Assertions.assertFalse(TechnicalID.same(new TechnicalIDImpl(null), new TechnicalIDImpl(2)));
    }

    @Test
    void otherIdNull() {
      Assertions.assertFalse(TechnicalID.same(new TechnicalIDImpl(1), new TechnicalIDImpl(null)));
    }

    @Test
    void idsDiffer() {
      Assertions.assertFalse(TechnicalID.same(new TechnicalIDImpl(1), new TechnicalIDImpl(2)));
    }

    @Test
    void idsSame() {
      Assertions.assertTrue(TechnicalID.same(new TechnicalIDImpl(1), new TechnicalIDImpl(1)));
    }
  }

  private record TechnicalIDImpl(Integer id) implements TechnicalID<Integer> {

    @Override
    public Integer getId() {
      return id;
    }
  }
}
