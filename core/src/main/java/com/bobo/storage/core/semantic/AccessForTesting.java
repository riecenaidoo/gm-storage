package com.bobo.storage.core.semantic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The access modifier of this method has been widened solely for internal use in testing scenarios.
 * Outside of the declaring class, it should not be called in application code.
 */
@Documented
@Target(ElementType.METHOD)
public @interface AccessForTesting {

  /**
   * @return the access {@link Modifier} this method was widened to for the purpose of testing.
   */
  Modifier value();

  @SuppressWarnings("unused")
  enum Modifier {
    /**
     * The expected case, to provide test utilities, such as {@code Mother} classes, that will be
     * defined in the same package.
     */
    PACKAGE_PRIVATE,
    /**
     * Only expected in cases where we would be needing to extend the parent class and override the
     * method, for stubbing, for example. Usages of this {@link Modifier} should be checked, because
     * they may actually just be {@link Modifier#PACKAGE_PRIVATE}.
     */
    PROTECTED,
    /**
     * Unexpected, but provided for completeness. Usages of this {@link Modifier} would be
     * refactoring targets.
     */
    PUBLIC
  }
}
