package me.ialistannen.simplecodetester.backend.services.checkrunning;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.exception.CheckRunningFailedException;
import me.ialistannen.simplecodetester.backend.exception.CompilationFailedException;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.submission.Submission;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CheckRunnerService implements DisposableBean, InitializingBean {

  @Value("${runner.classpath}")
  private String[] slaveClasspath;
  @Value("${runner.max-computation-time-seconds}")
  private long maxComputationTimeSeconds;

  private final Timer submissionDurationTimer;
  private final AtomicInteger queueLengthCounter;

  public CheckRunnerService() {
    this.submissionDurationTimer = Timer.builder("test_duration")
        .description("Duration of tests")
        .publishPercentileHistogram()
        .minimumExpectedValue(Duration.ofSeconds(2))
        .register(Metrics.globalRegistry);
    this.queueLengthCounter = new AtomicInteger();

    Gauge.builder("testing_queue_length", queueLengthCounter::get)
        .register(Metrics.globalRegistry);
  }

  /**
   * Checks a given submission and waits for a result.
   *
   * @param userId the id of the user
   * @param submission the {@link Submission} to check
   * @param checks the checks to run
   * @return the {@link SubmissionCheckResult}
   * @throws CompilationFailedException if the compilation failed
   * @throws CheckRunningFailedException if an unknown error occured
   */
  public SubmissionCheckResult check(String userId, Submission submission,
      List<CodeCheck> checks) {
    return null;
  }

  @Override
  public void afterPropertiesSet() {
  }

  @Override
  public void destroy() {

  }

}
