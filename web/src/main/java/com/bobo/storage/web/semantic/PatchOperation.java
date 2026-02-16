package com.bobo.storage.web.semantic;

/**
 * @see <a href="https://www.rfc-editor.org/rfc/rfc6902#section-4">Proposed Standard (RFC6902) JSON
 *     Patch > Operations</a>.
 */
public enum PatchOperation {
  ADD,
  REMOVE,
  REPLACE,
  MOVE,
  COPY,
  TEST
}
