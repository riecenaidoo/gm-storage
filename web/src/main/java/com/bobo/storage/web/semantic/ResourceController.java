package com.bobo.storage.web.semantic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.web.bind.annotation.RestController;

/**
 * A stereotyped {@link RestController} that services exactly one resource, and responds with a
 * consistent representation for the resource, when a response is necessary.
 *
 * <p>The name of the {@code Controller} must reflect the name of the resource it serves. If the
 * resource is a subresource, the name must be prefaced by the first parent of the resource.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
public @interface ResourceController {

  /**
   * @return the type of resource the {@code Controller} handles requests for.
   */
  Class<?> resource();

  /**
   * @return the representation of the resource the {@code Controller} will serve, when requested.
   */
  Class<?> respondsWith();
}
