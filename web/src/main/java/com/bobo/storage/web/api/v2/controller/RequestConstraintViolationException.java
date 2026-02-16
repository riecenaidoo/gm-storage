package com.bobo.storage.web.api.v2.controller;

/**
 * Thrown when a syntactically valid request was received, but failed one or more contract-defined
 * validation constraints.
 */
public class RequestConstraintViolationException extends RuntimeException {

  public RequestConstraintViolationException(String message) {
    super(message);
  }
}
