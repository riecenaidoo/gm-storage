package com.bobo.storage.core.song;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.scheduling.support.CronExpression;

/**
 * @param lookupCron The schedule to run {@link Song} {@link Lookup} jobs at, represented as a cron
 *     expression. See <a href="https://en.wikipedia.org/wiki/Cron#Cron_expression">Cron Expressions
 *     | Wikipedia</a>.
 *     <p>The {@link Lookup} job cycle was designed to be run as frequently as possible whilst still
 *     respecting {@link Provider} rate limits. The goal of doing this is to amortize processing and
 *     network load over the course of the day. Frequent {@link Lookup} cycles providing faster User
 *     feedback is a happy side effect.
 *     <p>Default of every minute was selected as a sane "cool off" period for respecting {@link
 *     Provider} rate limits. Exact rate limit for these public OEmbed endpoints does not seem to be
 *     documented anywhere. However, we have observed a rate limits being enforced on our system. It
 *     is likely dynamic.
 * @param recoveryCron The schedule to run {@link Lookup#recover()} jobs at, represented as a cron
 *     expression. See <a href="https://en.wikipedia.org/wiki/Cron#Cron_expression">Cron Expressions
 *     | Wikipedia</a>.
 *     <p>This job will always run once at system startup to recover {@link Lookup} jobs that may
 *     have been hung when the system was restarted/turned off.
 * @param maxJobSize The maximum number of {@link Song Songs} to process concurrently per {@link
 *     Lookup} job cycle.
 *     <p>Concurrency is backed by virtual threads, not platform threads, so this number is not
 *     limited by infrastructure thread count. The system can safely handle values in the hundreds
 *     to thousands. This, however, does not eliminate external I/O constraints.
 *     <p>The current bottleneck for increasing the number of maximum number of concurrent jobs are
 *     the rate limits from {@link Provider Providers} as well as the maximum concurrent streams
 *     allowed by their servers. See <a
 *     href="https://datatracker.ietf.org/doc/html/rfc7540#section-5.1.2">RFC7540 5.1.2 Stream
 *     Concurrency</a>.
 *     <p>A default of {@code 100} was chosen as it appears to be the default {@code
 *     SETTINGS_MAX_CONCURRENT_STREAMS} value for most HTTP/2 servers whilst still staying within
 *     {@link Provider} rate limits.
 * @param refreshThreshold The time before {@link Song} metadata is considered "stale" and should be
 *     refreshed as a {@link Duration} string, e.g. {@code 3d}, {@code 1w}, etc.
 *     <p>This makes a {@link Song} {@link Lookup} eligible for re-entry into the {@link Lookup}
 *     queue.
 * @param jobTimeoutThreshold The time before a {@link Lookup} job is considered to have "hung", and
 *     will now never complete, represented as a {@link Duration} string, e.g. {@code 180s}, {@code
 *     10m}, etc.
 *     <p>This makes a {@link Song} {@link Lookup} eligible for {@link Lookup#recover()}.
 * @param networkTimeout The time before a {@link Lookup} network request should be assumed to have
 *     failed and be abandoned, represented as a {@link Duration} string, e.g. {@code 5s}, etc.
 */
@ConfigurationProperties(prefix = "lookup")
public record LookupConfig(
    @Name("cron") String lookupCron,
    String recoveryCron,
    @Name("size") Integer maxJobSize,
    Duration refreshThreshold,
    Duration jobTimeoutThreshold,
    Duration networkTimeout) {

  /**
   * Construct with a custom configuration.
   *
   * @param lookupCron nullable. Defaults to every minute.
   * @param recoveryCron nullable. Defaults to every hour.
   * @param maxJobSize nullable, {@code > 0}. Defaults to {@code 100}.
   * @param refreshThreshold nullable, {@code > 0}. Defaults to {@code 3d}.
   * @param jobTimeoutThreshold nullable, {@code > 0}. Defaults to {@code 10m}.
   * @param networkTimeout nullable, {@code > 0}. Defaults to {@code 5s}.
   * @implNote {@link ConstructorBinding} is necessary to guide Spring to which overloaded
   *     constructor to use for binding, because {@link Record} is immutable, so Spring cannot fall
   *     back to binding via setters.
   */
  @ConstructorBinding
  public LookupConfig {
    lookupCron = (lookupCron == null) ? "0 * * * * *" : lookupCron;
    recoveryCron = (recoveryCron == null) ? "0 0 * * * *" : recoveryCron;
    maxJobSize = (maxJobSize == null) ? 100 : maxJobSize;
    refreshThreshold = (refreshThreshold == null) ? Duration.ofDays(3) : refreshThreshold;
    jobTimeoutThreshold =
        (jobTimeoutThreshold == null) ? Duration.ofMinutes(10) : jobTimeoutThreshold;
    networkTimeout = (networkTimeout == null) ? Duration.ofSeconds(5) : networkTimeout;

    if (!CronExpression.isValidExpression(lookupCron)) {
      throw new IllegalArgumentException("lookupCron of %s is invalid.".formatted(lookupCron));
    }
    if (!CronExpression.isValidExpression(recoveryCron)) {
      throw new IllegalArgumentException("recoveryCron of %s is invalid.".formatted(recoveryCron));
    }
    if (maxJobSize <= 0) {
      throw new IllegalArgumentException(
          "maxJobSize of %s is invalid. Must be greater than zero.".formatted(maxJobSize));
    }
    if (!refreshThreshold.isPositive()) {
      throw new IllegalArgumentException(
          "refreshThreshold of %s is invalid. Must be greater than zero."
              .formatted(refreshThreshold));
    }
    if (!jobTimeoutThreshold.isPositive()) {
      throw new IllegalArgumentException(
          "jobTimeoutThreshold of %s is invalid. Must be greater than zero."
              .formatted(jobTimeoutThreshold));
    }
    if (!networkTimeout.isPositive()) {
      throw new IllegalArgumentException(
          "networkTimeout of %s is invalid. Must be greater than zero.".formatted(networkTimeout));
    }
  }

  /** Construct with the default configuration. */
  public LookupConfig() {
    this(null, null, null, null, null, null);
  }

  /**
   * @see #getLookupCron()
   */
  @Override
  public String lookupCron() {
    return lookupCron;
  }

  public CronExpression getLookupCron() {
    return CronExpression.parse(lookupCron);
  }

  /**
   * @see #getRecoveryCron()
   */
  @Override
  public String recoveryCron() {
    return recoveryCron;
  }

  public CronExpression getRecoveryCron() {
    return CronExpression.parse(recoveryCron);
  }
}
