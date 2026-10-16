package com.bobo.storage.web.semantic;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.web.bind.annotation.RestController;

/**
 * A stereotyped {@link RestController} that implements a {@link ResourceAPI}.
 *
 * <p>Should contain only simple control flow logic to delegate to the relevant Core service(s)
 * needed to perform the requested operation, mapping the result accordingly into a {@link
 * ResponseSchema}.
 *
 * @implSpec Implementation should be {@code package-private} because it is expected to be managed
 *     by Spring, and never instantiated or referenced elsewhere in application source code.
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RestController
public @interface ResourceController {}
