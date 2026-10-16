package com.bobo.storage.web.semantic;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.web.bind.annotation.RestController;

/**
 * A {@code package-private} implementation of a {@link ResourceAPI} managed by Spring, and never
 * instantiated or referenced elsewhere in application source code.
 *
 * @implSpec The Controller is responsible for fulfilling an API contract. It should only have
 *     simple control flow logic to delegate to the relevant Core service(s) needed to perform the
 *     requested operation, mapping the result accordingly into a {@link ResponseSchema}.
 */
@Target({TYPE})
@Retention(RetentionPolicy.RUNTIME)
@RestController
public @interface ResourceController {}
