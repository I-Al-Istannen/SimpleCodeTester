package me.ialistannen.simplecodetester.backend.services.checkrunning;

import static java.util.stream.Collectors.toList;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.exception.CheckRunningFailedException;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.submission.ImmutableCompleteTask;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.submission.Submission;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CheckRunnerService {

  private final TaskQueue queue;
  private final Timer submissionDurationTimer;

  public CheckRunnerService(TaskQueue queue) {
    this.queue = queue;

    this.submissionDurationTimer = Timer.builder("test_duration")
        .description("Duration of tests")
        .publishPercentileHistogram()
        .minimumExpectedValue(Duration.ofSeconds(2))
        .register(Metrics.globalRegistry);

    Gauge.builder("testing_queue_length", queue::size)
        .register(Metrics.globalRegistry);
  }

  /**
   * Checks a given submission and waits for a result.
   *
   * @param userId the id of the user
   * @param submission the {@link Submission} to check
   * @param checks the checks to run
   * @return the {@link Result}
   * @throws CheckRunningFailedException if an unknown error occurred
   */
  public Result check(String userId, Submission submission,
      List<CodeCheck> checks) {
    Map<String, String> fileMap = new HashMap<>(submission.files());
    submission = ImmutableSubmission
        .copyOf(submission)
        .withFiles(fileMap);

    CompletableFuture<Result> result = queue.addTask(
        ImmutableCompleteTask.builder()
            .userId(userId)
            .submission(submission)
            .addAllChecks(checks.stream().map(CodeCheck::getText).collect(toList()))
            .build()
    );

    Instant start = Instant.now();
    try {
      return result.get(5, TimeUnit.MINUTES);
    } catch (InterruptedException | ExecutionException e) {
      log.warn("Check running failed (" + userId + ")", e);
      throw new CheckRunningFailedException(
          "Running your check failed :/ Feel free to report this."
      );
    } catch (TimeoutException e) {
      log.warn("Dropped task for ({}) due to a timeout", userId);
      queue.removeTaskForUser(userId);
      throw new CheckRunningFailedException(
          "Your task was dropped due to an internal timeout. Please try again in a few seconds..."
      );
    } finally {
      submissionDurationTimer.record(Duration.between(start, Instant.now()));
    }
  }
}
