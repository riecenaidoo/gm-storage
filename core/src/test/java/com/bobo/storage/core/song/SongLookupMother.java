package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.EntityMother;
import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

public class SongLookupMother implements EntityMother<SongLookup> {

  private final Random random;

  private Supplier<Integer> ids;

  private Supplier<Song> songs;

  private Supplier<SongLookup.JobStatus> statuses;

  private Supplier<Instant> lastLookup;

  private Supplier<Instant> lastModified;

  private Supplier<Short> failed;

  /**
   * Unless otherwise configured, a {@link SongLookupMother} uses randomness to generate mock data.
   * If your test has an instance of {@link Random}, you should share it with the {@link
   * SongLookupMother}.
   *
   * <p>This is also provided if you need to {@code seed} the generation of the mock data for
   * reproducing failures.
   *
   * @param random to use when generating mock data.
   */
  public SongLookupMother(Random random) {
    this.random = random;

    withSongs();
  }

  /**
   * While the default constructor is always provided, prefer {@link
   * SongLookupMother#SongLookupMother(Random)} where possible.
   */
  @SuppressWarnings("unused")
  public SongLookupMother() {
    this(new Random());
  }

  @Override
  public SongLookup get() {
    SongLookup lookup = new SongLookup(songs.get());

    if (ids != null) {
      EntityMother.setId(lookup, ids.get());
    }
    if (statuses != null) {
      lookup.setStatus(statuses.get());
    }
    if (lastLookup != null) {
      lookup.setLastLookup(lastLookup.get());
    }
    if (lastModified != null) {
      lookup.setLastModified(lastModified.get());
    }
    if (failed != null) {
      lookup.setFailed(failed.get());
    }

    return lookup;
  }

  @Override
  public SongLookup setId(SongLookup lookup) {
    return EntityMother.setId(lookup, random.nextInt());
  }

  @Override
  public SongLookupMother withAll() {
    return withIds().withSongs().withStatuses().withLastLookup().withLastModified().withFailed();
  }

  @Override
  public SongLookupMother withIds(Supplier<Integer> ids) {
    this.ids = ids;
    return this;
  }

  @Override
  public SongLookupMother withIds() {
    return withIds(this.random::nextInt);
  }

  public SongLookupMother withSongs(Supplier<Song> songs) {
    Objects.requireNonNull(
        songs,
        """
        The Supplier of a required field in a Mother class can never be null.
        Invoke #withSongs if you need to reset a previous #withSongs(Supplier) configuration.\
        """);
    this.songs = songs;
    return this;
  }

  public SongLookupMother withSongs() {
    return this.withSongs(new SongMother(random).withIds());
  }

  public SongLookupMother withStatuses(Supplier<SongLookup.JobStatus> statuses) {
    this.statuses = statuses;
    return this;
  }

  public SongLookupMother withStatuses() {
    return withStatuses(
        () ->
            SongLookup.JobStatus.values()[
                this.random.nextInt(0, SongLookup.JobStatus.values().length)]);
  }

  public SongLookupMother withLastLookup(Supplier<Instant> lastLookup) {
    this.lastLookup = lastLookup;
    return this;
  }

  public SongLookupMother withLastLookup() {
    return withLastLookup(() -> Instant.now().minusSeconds(this.random.nextLong()));
  }

  public SongLookupMother withLastModified(Supplier<Instant> lastModified) {
    this.lastModified = lastModified;
    return this;
  }

  public SongLookupMother withLastModified() {
    return withLastModified(() -> Instant.now().minusSeconds(this.random.nextLong()));
  }

  public SongLookupMother withFailed(Supplier<Short> failed) {
    this.failed = failed;
    return this;
  }

  public SongLookupMother withFailed() {
    return withFailed(() -> (short) this.random.nextInt());
  }
}
