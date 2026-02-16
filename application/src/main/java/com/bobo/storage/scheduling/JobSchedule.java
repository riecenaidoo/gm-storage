package com.bobo.storage.scheduling;

import com.bobo.storage.core.song.Lookup;
import com.bobo.storage.core.song.LookupConfig;
import com.bobo.storage.core.song.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

/** The schedule of jobs the system should run, and when. */
@Component
public class JobSchedule implements SchedulingConfigurer {

  private static final Logger log = LoggerFactory.getLogger(JobSchedule.class);

  private final SongService songs;

  private final Lookup lookup;

  private final LookupConfig lookupConfig;

  public JobSchedule(SongService songs, Lookup lookup, LookupConfig lookupConfig) {
    this.songs = songs;
    this.lookup = lookup;
    this.lookupConfig = lookupConfig;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void genesis() {
    songs.verify();
    lookup.recover();
    log.info("Genesis: My works end, so that others may begin.");
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {
    registrar.addCronTask(lookup::lookup, lookupConfig.lookupCron());
    registrar.addCronTask(lookup::recover, lookupConfig.recoveryCron());

    String jobSchedule =
        """
        Chronos: Time moves forward, and the schedule is known...
          Lookup — {}
          Lookup Recovery — {}\
        """;
    log.debug(jobSchedule, lookupConfig.lookupCron(), lookupConfig.recoveryCron());
  }
}
