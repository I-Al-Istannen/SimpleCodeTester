package me.ialistannen.simplecodetester.backend.services.checkrunning;

import static java.util.stream.Collectors.toList;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.exception.CheckRunningFailedException;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.submission.ImmutableCompleteTask;
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
    CompletableFuture<Result> result = queue.addTask(
        ImmutableCompleteTask.builder()
            .userId(userId)
            .submission(submission)
            .addAllChecks(checks.stream().map(CodeCheck::getText).collect(toList()))
            .build()
    );

    try {
      System.out.println("Waiting for my tiiimmee");
      return result.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new CheckRunningFailedException(userId, e);
    }
  }
}
