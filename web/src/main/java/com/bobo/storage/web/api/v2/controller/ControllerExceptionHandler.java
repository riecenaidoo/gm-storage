package com.bobo.storage.web.api.v2.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * "Our goal is to not handle exceptions explicitly in Controller methods where possible. They are a
 * cross-cutting concern better handled separately in dedicated code."
 *
 * @see <a href="https://spring.io/blog/2013/11/01/exception-handling-in-spring-mvc">Exception
 *     Handling in Spring MVC</a>
 */
@ControllerAdvice(name = "v2Handler")
public class ControllerExceptionHandler {

  /**
   * Returns {@code 400 Bad Request} if the request violates the contractual constraints defined in
   * the API — even if it was successfully received and parsed.
   *
   * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#section-15.5.1">400 Bad Request</a>
   */
  @ExceptionHandler(RequestConstraintViolationException.class)
  public ProblemDetail requestConstraintViolation(
      RequestConstraintViolationException requestConstraintViolation) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problemDetail.setDetail(requestConstraintViolation.getMessage());
    problemDetail.setType(URI.create("https://www.rfc-editor.org/rfc/rfc9110.html#section-15.5.1"));

    return problemDetail;
  }

  /**
   * Returns {@code 404 Not Found} if a {@code Resource} in the request path did not exist.
   *
   * <p>
   *
   * <ul>
   *   <li>e.g. {@code PATCH /playlists/1} requesting a {@code PATCH} operation on the {@code
   *       Playlist} with the id {@code 1}, but it does not exist.
   *   <li>e.g. {@code /playlists/1/songs/4} implying the {@code Playlist} with the id {@code 1}
   *       exists, but it did not.
   * </ul>
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail resourceNotFound(ResourceNotFoundException resourceNotFound) {
    String detail =
        "The %s(ID:%s) resource in the request path does not exist."
            .formatted(resourceNotFound.getResourceName(), resourceNotFound.getIdentifier());

    return notFound(detail);
  }

  /**
   * Returns {@code 404 Not Found} if a {@code Subresource} exists, but does not match the scope
   * implied by the parent {@code Resource} in the URI.
   *
   * <ol>
   *   <li>e.g. {@code DELETE /playlists/1/songs/4} requesting a {@code DELETE} operation on the
   *       {@code PlaylistSong} with the id {@code 4}, which does exist, but not as a subresource of
   *       the {@code Playlist} with the id {@code 1}.
   * </ol>
   */
  @ExceptionHandler(SubresourceMismatchException.class)
  public ProblemDetail subresourceMismatch(SubresourceMismatchException subresourceMismatch) {
    String detail =
        "The %s(ID:%s) resource in the request path does not exist within the %s(ID:%s) resource."
            .formatted(
                subresourceMismatch.getSubResourceName(),
                subresourceMismatch.getSubResourceIdentifier(),
                subresourceMismatch.getResourceName(),
                subresourceMismatch.getIdentifier());

    return notFound(detail);
  }

  /**
   * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#section-15.5.5">404 Not Found</a>
   */
  private ProblemDetail notFound(String detail) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    problemDetail.setDetail(detail);
    problemDetail.setType(URI.create("https://www.rfc-editor.org/rfc/rfc9110.html#section-15.5.5"));

    return problemDetail;
  }
}
