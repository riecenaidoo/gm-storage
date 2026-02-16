package com.bobo.storage.core.playlist;

import com.bobo.storage.core.semantic.EntityMother;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

public class PlaylistMother implements EntityMother<Playlist> {

  private final Random random;

  private Supplier<Integer> ids;

  private Supplier<String> names;

  /**
   * Unless otherwise configured, a {@code PlaylistMother} uses randomness to generate mock data. If
   * your test has an instance of {@link Random}, you should share it with the {@code
   * PlaylistMother}.
   *
   * <p>This is also provided if you need to {@code seed} the generation of the mock data for
   * reproducing failures.
   *
   * @param random to use when generating mock data.
   */
  public PlaylistMother(Random random) {
    this.random = random;
  }

  /**
   * While the default constructor is always provided, prefer {@link
   * PlaylistMother#PlaylistMother(Random)} where possible.
   */
  @SuppressWarnings("unused")
  public PlaylistMother() {
    this(new Random());
  }

  @Override
  public Playlist get() {
    Playlist playlist = new Playlist();

    Integer id = Objects.isNull(ids) ? null : ids.get();
    String name = Objects.isNull(names) ? "" : names.get();

    EntityMother.setId(playlist, id);
    playlist.setName(name);

    return playlist;
  }

  @Override
  public PlaylistMother withAll() {
    return this.withIds().withNames();
  }

  @Override
  public Playlist setId(Playlist playlist) {
    return EntityMother.setId(playlist, random.nextInt());
  }

  @Override
  public PlaylistMother withIds(Supplier<Integer> ids) {
    this.ids = ids;
    return this;
  }

  @Override
  public PlaylistMother withIds() {
    return withIds(this.random::nextInt);
  }

  public PlaylistMother withNames(Supplier<String> names) {
    this.names = names;
    return this;
  }

  public PlaylistMother withNames() {
    return withNames(() -> Long.toString(this.random.nextLong()));
  }
}
