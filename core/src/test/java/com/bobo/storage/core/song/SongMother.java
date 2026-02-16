package com.bobo.storage.core.song;

import com.bobo.storage.core.semantic.EntityMother;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;

public class SongMother implements EntityMother<Song> {

  private final Random random;

  private Supplier<Integer> ids;

  private Supplier<String> urls;

  private Supplier<LocalDateTime> lastLookups;

  private Supplier<String> titles;

  private Supplier<String> artists;

  private Supplier<String> thumbnailUrls;

  /**
   * Unless otherwise configured, a {@code SongMother} uses randomness to generate mock data. If
   * your test has an instance of {@link Random}, you should share it with the {@code SongMother}.
   *
   * <p>This is also provided if you need to {@code seed} the generation of the mock data for
   * reproducing failures.
   *
   * @param random to use when generating mock data.
   */
  public SongMother(Random random) {
    this.random = random;

    withUrls();
  }

  /**
   * While the default constructor is always provided, prefer {@link SongMother#SongMother(Random)}
   * where possible.
   */
  @SuppressWarnings("unused")
  public SongMother() {
    this(new Random());
  }

  @Override
  public Song get() {
    Song song = new Song();

    Integer id = Objects.isNull(ids) ? null : ids.get();
    assert urls != null;
    String url = urls.get();
    LocalDateTime lastLookup = Objects.isNull(lastLookups) ? null : lastLookups.get();
    String title = Objects.isNull(titles) ? null : titles.get();
    String artist = Objects.isNull(artists) ? null : artists.get();
    String thumbnailUrl = Objects.isNull(thumbnailUrls) ? null : thumbnailUrls.get();

    EntityMother.setId(song, id);
    song.setUrl(url);
    song.setLastLookup(lastLookup);
    song.setTitle(title);
    song.setArtist(artist);
    song.setThumbnailUrl(thumbnailUrl);

    return song;
  }

  @Override
  public SongMother withAll() {
    return this.withIds()
        .withUrls()
        .withLastLookups()
        .withTitles()
        .withArtists()
        .withThumbnailUrls();
  }

  @Override
  public Song setId(Song song) {
    return EntityMother.setId(song, random.nextInt());
  }

  @Override
  public SongMother withIds(Supplier<Integer> ids) {
    this.ids = ids;
    return this;
  }

  @Override
  public SongMother withIds() {
    return withIds(this.random::nextInt);
  }

  public SongMother withUrls(Supplier<String> urls) {
    Objects.requireNonNull(
        urls,
        """
        The Supplier of a required field in a Mother class can never be null.
        Invoke #withUrls if you need to reset a previous #withUrl(Supplier) configuration.\
        """);
    this.urls = urls;
    return this;
  }

  public SongMother withUrls() {
    return this.withUrls(
        () -> String.format(Locale.ROOT, "https://mock-%d.test", random.nextLong()));
  }

  public SongMother withLastLookups(Supplier<LocalDateTime> lastLookups) {
    this.lastLookups = lastLookups;
    return this;
  }

  public SongMother withLastLookups() {
    return withLastLookups(() -> LocalDateTime.now().minusSeconds(Math.abs(random.nextLong())));
  }

  public SongMother withTitles(Supplier<String> titles) {
    this.titles = titles;
    return this;
  }

  public SongMother withTitles() {
    return withTitles(() -> Long.toString(this.random.nextLong()));
  }

  public SongMother withArtists(Supplier<String> artists) {
    this.artists = artists;
    return this;
  }

  public SongMother withArtists() {
    return withArtists(() -> Long.toString(this.random.nextLong()));
  }

  public SongMother withThumbnailUrls(Supplier<String> thumbnailUrls) {
    this.thumbnailUrls = thumbnailUrls;
    return this;
  }

  public SongMother withThumbnailUrls() {
    return withThumbnailUrls(
        () -> String.format(Locale.ROOT, "https://mock-%d.test", random.nextLong()));
  }
}
