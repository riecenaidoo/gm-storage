package com.bobo.storage.core.song;

import com.bobo.semantic.TechnicalID;
import com.bobo.storage.core.semantic.DomainEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The job state for a {@link Song} {@link Lookup}. */
@Entity
public class SongLookup extends DomainEntity {

  private static final Logger log = LoggerFactory.getLogger(SongLookup.class);

  /**
   * The maximum number of times to retry failed attempts at {@link Lookup} for a {@link Song}.
   *
   * @see #failed()
   */
  private static final int MAX_ATTEMPT = 3;

  /**
   * Every {@link Song} has a single lookup job associated with it.
   *
   * <p>They share the same {@link TechnicalID}.
   */
  @OneToOne(optional = false)
  @MapsId
  private Song song;

  /** The {@link JobStatus} for the {@link Lookup} job of the {@link Song}. */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private JobStatus status;

  /**
   * The last time the {@link Song} was looked up. Specifically, the last time the {@link Lookup}
   * job for the {@link Song} ran to completion — whether it was successful or not.
   *
   * @apiNote This is queried in combination with {@link #status} to derive the {@link Lookup} job
   *     queue.
   * @implNote Nullable, as a {@link Song} can never have been looked up before.
   * @see #lastModified
   */
  @Column private Instant lastLookup;

  /**
   * The last time this was modified.
   *
   * @apiNote This tracks any modification event, and is queried in combination with {@link #status}
   *     to find hung jobs.
   * @see #lastLookup
   */
  @Column(nullable = false)
  private Instant lastModified;

  /**
   * The number of times this lookup has failed.
   *
   * @implNote Keeping this state allows us to have a retry mechanism, but back-off if there are
   *     repeated failures.
   */
  @Column(nullable = false)
  private short failed;

  /**
   * @see DomainEntity#DomainEntity()
   */
  protected SongLookup() {}

  public SongLookup(Song song) {
    // Existence Validation
    Objects.requireNonNull(song, "Song argument cannot not be null.");
    // State Validation
    if (song.getId() == null) {
      throw new IllegalArgumentException("Song argument must have an assigned TechnicalID.");
    }
    this.song = song;

    this.status = JobStatus.PENDING;
    this.lastModified = Instant.now();
    this.failed = 0;
  }

  public Song getSong() {
    return song;
  }

  void start() {
    if (status.equals(JobStatus.PROCESSING)) {
      throw new IllegalStateException(
          "Cannot start %s#Lookup. It is already in progress.".formatted(song.log()));
    }

    if (status.equals(JobStatus.DONE)) {
      log.trace(
          "{}#Lookup was successfully looked-up and has now re-entered the lookup queue.",
          song.log());
      failed = 0;
    }
    status = JobStatus.PROCESSING;
    lastModified = Instant.now();
  }

  /**
   * @implNote Failed jobs will be retried up to a limit.
   */
  void failed() {
    failed++;
    if (failed > MAX_ATTEMPT) {
      this.status = JobStatus.INVALID;
      log.warn(
          "Sentinel: {} exceeded failure threshold ({}). No further.", this.log(), MAX_ATTEMPT);
    } else {
      this.status = JobStatus.PENDING;
    }
    lastLookup = Instant.now();
    lastModified = Instant.now();
  }

  void finish(JobStatus status) {
    if (status.equals(JobStatus.PROCESSING)) {
      throw new IllegalArgumentException(
          "Cannot finish %s#Lookup with a status of '%s'."
              .formatted(song.log(), JobStatus.PROCESSING));
    }

    this.status = status;
    lastLookup = Instant.now();
    lastModified = Instant.now();
  }

  enum JobStatus {
    /** The lookup is awaiting further processing. */
    PENDING,
    /**
     * The lookup is being processed.
     *
     * @implNote This status should be used as a distributed locking mechanism to ensure two jobs do
     *     not attempt to perform the same lookup.
     */
    PROCESSING,
    /** The lookup cycle has completed, and no further processing is required. */
    DONE,
    /**
     * There are irrecoverable errors occurring when looking up this song.
     *
     * @implNote This status should be used to filter out lookups from the queue.
     */
    INVALID
  }
}
