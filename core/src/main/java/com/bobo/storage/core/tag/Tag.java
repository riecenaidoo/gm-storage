package com.bobo.storage.core.tag;

import static com.bobo.storage.core.semantic.Normalisations.truncateToSize;

import com.bobo.storage.core.semantic.DomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.util.Objects;

/** A unique keyword or term to classify or organise other resources. */
@Entity
public class Tag extends DomainEntity {

  /**
   * The unique keyword, restricted to {@code 256} characters.
   *
   * @implNote Keywords must be unique terms, and uniqueness is compared case-insensitively.
   */
  @Column(nullable = false, unique = true, length = 256)
  private String tag;

  /**
   * @see DomainEntity#DomainEntity()
   */
  protected Tag() {}

  /**
   * @param tag not {@code null} or {@link String#isBlank() blank}.
   * @throws IllegalArgumentException if any constraints are violated.
   */
  public Tag(String tag) {
    this.tag = validatedTag(tag);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Tag tag1 = (Tag) o;
    return Objects.equals(tag, tag1.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(tag);
  }

  /**
   * @see #tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Validate and normalise a {@code tag}.
   *
   * @see #tag
   */
  private String validatedTag(String tag) {
    if (tag == null || tag.isBlank()) {
      throw new IllegalArgumentException("Tag is required. Cannot be null or blank.");
    }
    return truncateToSize(256).apply(tag.trim()).toLowerCase();
  }

  /**
   * @see #tag
   */
  public void setTag(String tag) {
    this.tag = validatedTag(tag);
  }
}
