package com.bobo.storage.core.song;

import com.bobo.semantic.UnitTest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.scheduling.support.CronExpression;

/**
 * - System configuration is a User-facing boundary. The API should be well-thought-out and guarded.
 * - Convention over configuration. We should provide opinionated defaults. - Leverage warning logs
 * if a configuration could cause issues, but otherwise allow user agency. - An invalid
 * configuration should fail fast.
 */
@UnitTest(LookupConfig.class)
class LookupConfigTest {

  @Nested
  class Construction {

    /**
     * @implNote This test is to flag if additional constructors are provided but the test suite
     *     hasn't been updated.
     */
    @Test
    void controlledConstruction() {
      Assertions.assertEquals(
          2,
          LookupConfig.class.getDeclaredConstructors().length,
          "LookupConfig provides Canonical and Nullary Constructors.");
    }

    /**
     * @see LookupConfig#LookupConfig()
     */
    @Test
    void nullaryConstructor() {
      LookupConfig config = Assertions.assertDoesNotThrow(() -> new LookupConfig());
      assertConfigInitialised(config);
    }

    /**
     * @see LookupConfig#LookupConfig(String, String, Integer, Duration, Duration, Duration)
     */
    @Test
    void canonicalConstructorNullable() {
      LookupConfig config =
          Assertions.assertDoesNotThrow(() -> new LookupConfig(null, null, null, null, null, null));
      assertConfigInitialised(config);
    }

    private static void assertConfigInitialised(LookupConfig config) {
      Assertions.assertNotNull(config.lookupCron());
      Assertions.assertNotNull(config.recoveryCron());

      Assertions.assertNotNull(config.maxJobSize());

      Assertions.assertNotNull(config.refreshThreshold());
      Assertions.assertNotNull(config.jobTimeoutThreshold());

      Assertions.assertNotNull(config.networkTimeout());
    }
  }

  /**
   * @see LookupConfig#lookupCron()
   * @see LookupConfig#LookupConfig(String, String, Integer, Duration, Duration, Duration)
   *     LookupConfig
   */
  @Nested
  class LookupCron {

    @ValueSource(strings = {"0 0 * * * *"})
    @ParameterizedTest
    void lookupCron(String validCron) {
      LookupConfig lookupConfig = new LookupConfig(validCron, null, null, null, null, null);

      Assertions.assertEquals(validCron, lookupConfig.lookupCron());
    }

    @Test
    void defaultsToPerMinute() {
      LookupConfig lookupConfig = new LookupConfig();

      CronExpression cron = lookupConfig.getLookupCron();

      LocalDateTime start = LocalDateTime.MIN;
      LocalDateTime end = cron.next(start);

      Assertions.assertEquals(
          Duration.ofMinutes(1), Duration.between(start, end), "Does not default to every minute.");
    }

    @ValueSource(strings = {"minute"})
    @ParameterizedTest
    void validatedCronExpression(String invalidCron) {
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> new LookupConfig(invalidCron, null, null, null, null, null),
          "Allows invalid cron expressions.");
    }
  }

  @Nested
  class RecoveryCron {

    @ValueSource(strings = {"0 0 0 * * *"})
    @ParameterizedTest
    void recoveryCron(String validCron) {
      LookupConfig lookupConfig = new LookupConfig(null, validCron, null, null, null, null);

      Assertions.assertEquals(validCron, lookupConfig.recoveryCron());
    }

    @Test
    void defaultsToPerHour() {
      LookupConfig lookupConfig = new LookupConfig();

      CronExpression cron = lookupConfig.getRecoveryCron();

      LocalDateTime start = LocalDateTime.MIN;
      LocalDateTime end = cron.next(start);

      Assertions.assertEquals(
          Duration.ofHours(1), Duration.between(start, end), "Does not default to every hour.");
    }

    @ValueSource(strings = {"hour"})
    @ParameterizedTest
    void validatedCronExpression(String invalidCron) {
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> new LookupConfig(null, invalidCron, null, null, null, null),
          "Allows invalid cron expressions.");
    }
  }

  @Nested
  class MaxJobSize {

    @ValueSource(ints = {1, 10, 50, 100})
    @ParameterizedTest
    void maxJobSize(int validSize) {
      LookupConfig lookupConfig = new LookupConfig(null, null, validSize, null, null, null);

      Assertions.assertEquals(validSize, lookupConfig.maxJobSize());
    }

    @Test
    void defaultsTo100() {
      LookupConfig lookupConfig = new LookupConfig();

      Assertions.assertEquals(100, lookupConfig.maxJobSize(), "Does not default to 100.");
    }

    @ValueSource(ints = {-100, 0})
    @ParameterizedTest()
    void greaterThanZero(int invalidSize) {
      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> new LookupConfig(null, null, invalidSize, null, null, null),
          "Allows values less than or equal to zero.");
    }
  }

  @Nested
  class RefreshThreshold {

    @ParameterizedTest
    @MethodSource("validThresholds")
    void refreshThreshold(Duration validThreshold) {
      LookupConfig lookupConfig = new LookupConfig(null, null, null, validThreshold, null, null);

      Assertions.assertEquals(validThreshold, lookupConfig.refreshThreshold());
    }

    @Test
    void defaultsToThreeDays() {
      LookupConfig lookupConfig = new LookupConfig();

      Assertions.assertEquals(
          Duration.ofDays(3), lookupConfig.refreshThreshold(), "Does not default to 3d.");
    }

    @ParameterizedTest
    @MethodSource("invalidThresholds")
    void greaterThanZero(Duration invalidThreshold) {

      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> new LookupConfig(null, null, null, invalidThreshold, null, null),
          "Allows values less than or equal to zero.");
    }

    private static Stream<Duration> validThresholds() {
      return Stream.of(Duration.ofDays(1), Duration.ofDays(7), Duration.ofDays(31));
    }

    private static Stream<Duration> invalidThresholds() {
      return Stream.of(Duration.ZERO, Duration.ofDays(-1));
    }
  }

  @Nested
  class JobTimeoutThreshold {

    @ParameterizedTest
    @MethodSource("validThresholds")
    void jobTimeoutThreshold(Duration validThreshold) {
      LookupConfig lookupConfig = new LookupConfig(null, null, null, null, validThreshold, null);

      Assertions.assertEquals(validThreshold, lookupConfig.jobTimeoutThreshold());
    }

    @Test
    void defaultsToTenMinutes() {
      LookupConfig lookupConfig = new LookupConfig();

      Assertions.assertEquals(
          Duration.ofMinutes(10), lookupConfig.jobTimeoutThreshold(), "Does not default to 10m.");
    }

    @ParameterizedTest
    @MethodSource("invalidThresholds")
    void greaterThanZero(Duration invalidThreshold) {

      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> new LookupConfig(null, null, null, null, invalidThreshold, null),
          "Allows values less than or equal to zero.");
    }

    private static Stream<Duration> validThresholds() {
      return Stream.of(Duration.ofMinutes(1), Duration.ofMinutes(7), Duration.ofMinutes(31));
    }

    private static Stream<Duration> invalidThresholds() {
      return Stream.of(Duration.ZERO, Duration.ofMinutes(-1));
    }
  }

  @Nested
  class NetworkTimeout {

    @ParameterizedTest
    @MethodSource("validThresholds")
    void networkTimeout(Duration validThreshold) {
      LookupConfig lookupConfig = new LookupConfig(null, null, null, null, null, validThreshold);

      Assertions.assertEquals(validThreshold, lookupConfig.networkTimeout());
    }

    @Test
    void defaultsToFiveSeconds() {
      LookupConfig lookupConfig = new LookupConfig();

      Assertions.assertEquals(
          Duration.ofSeconds(5), lookupConfig.networkTimeout(), "Does not default to 5s.");
    }

    @ParameterizedTest
    @MethodSource("invalidThresholds")
    void greaterThanZero(Duration invalidThreshold) {

      Assertions.assertThrows(
          IllegalArgumentException.class,
          () -> new LookupConfig(null, null, null, null, null, invalidThreshold),
          "Allows values less than or equal to zero.");
    }

    private static Stream<Duration> validThresholds() {
      return Stream.of(Duration.ofSeconds(1), Duration.ofSeconds(7), Duration.ofSeconds(31));
    }

    private static Stream<Duration> invalidThresholds() {
      return Stream.of(Duration.ZERO, Duration.ofSeconds(-1));
    }
  }
}
