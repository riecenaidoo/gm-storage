package com.bobo.storage.scheduling;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.bobo.semantic.UnitTest;
import com.bobo.storage.core.song.Lookup;
import com.bobo.storage.core.song.LookupConfig;
import com.bobo.storage.core.song.SongService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@UnitTest(JobSchedule.class)
@ExtendWith(MockitoExtension.class)
class JobScheduleTest {

  JobSchedule jobSchedule;

  private final LookupConfig lookupConfig = new LookupConfig();

  @Mock SongService songs;

  @Mock Lookup lookup;

  @BeforeEach
  void given() {
    jobSchedule = new JobSchedule(songs, lookup, lookupConfig);
  }

  /**
   * Ensure we these recovery mechanisms fire on startup.
   *
   * @see JobSchedule#genesis()
   */
  @Test
  void genesis() {
    // When
    jobSchedule.genesis();
    // Then
    verify(songs, times(1)).verify();
    verify(lookup, times(1)).recover();
  }

  /**
   * Ensure cron jobs are registered.
   *
   * @see JobSchedule#configureTasks(ScheduledTaskRegistrar)
   */
  @Test
  void configureTask(@Mock ScheduledTaskRegistrar registrar) {
    // When
    jobSchedule.configureTasks(registrar);
    // Then
    verify(registrar, times(1)).addCronTask(any(Runnable.class), eq(lookupConfig.lookupCron()));
    verify(registrar, times(1)).addCronTask(any(Runnable.class), eq(lookupConfig.recoveryCron()));
  }
}
