package me.ialistannen.simplecodetester.backend.services.checkrunning;

import static java.util.stream.Collectors.toList;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.exception.CheckAlreadyRunningException;
import me.ialistannen.simplecodetester.backend.exception.CheckRunningFailedException;
import me.ialistannen.simplecodetester.backend.exception.CompilationFailedException;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.CheckResult.ResultType;
import me.ialistannen.simplecodetester.checks.ImmutableCheckResult;
import me.ialistannen.simplecodetester.checks.ImmutableSubmissionCheckResult;
import me.ialistannen.simplecodetester.checks.SubmissionCheckResult;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.execution.MessageClient;
import me.ialistannen.simplecodetester.execution.master.SlaveManager;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.ProtocolMessage;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.CompilationFailed;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.DyingMessage;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveComputationTookTooLong;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveDiedWithUnknownError;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SlaveTimedOut;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.StartedCheck;
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.submission.Submission;
import me.ialistannen.simplecodetester.util.StringOutputStream;
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

  private SlaveManager slaveManager;
  private final Map<String, RunningCheck> runningChecks;
  private final Timer submissionDurationTimer;
  private final AtomicInteger queueLengthCounter;

  public CheckRunnerService() {
    this.runningChecks = new ConcurrentHashMap<>();
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
    queueLengthCounter.incrementAndGet();
    // TODO: 30.01.19 Allow running in parallel?
    synchronized (this) {
      if (runningChecks.containsKey(userId)) {
        queueLengthCounter.decrementAndGet();
        throw new CheckAlreadyRunningException();
      }

      Instant start = Instant.now();
      Semaphore semaphore = new Semaphore(0);
      RunningCheck check = new RunningCheck(semaphore);
      runningChecks.put(userId, check);

      List<String> checksToRun = checks.stream()
          .map(CodeCheck::getText)
          // run longer tests later as they are more likely to time out
          .sorted(Comparator.comparingInt(String::length))
          .collect(toList());

      Process process = slaveManager.runSubmission(submission, checksToRun, userId);

      try {
        StringOutputStream outputStream = new StringOutputStream();
        Thread thread = new Thread(() -> {
          try (InputStream out = process.getErrorStream()) {
            out.transferTo(outputStream);
          } catch (IOException e) {
            log.info("Reading from runner threw exception", e);
          }
        });
        thread.start();
        process.waitFor(maxComputationTimeSeconds, TimeUnit.SECONDS);
        thread.interrupt();
        // Wait after process death
        if (!semaphore.tryAcquire(2, TimeUnit.SECONDS)) {
          throw new CheckRunningFailedException(
              "The slave is dead and I got no message. Output:\n" + outputStream.toString()
                  + "\n" + (process.isAlive() ? "Process still running" : process.exitValue())
          );
        }
      } catch (InterruptedException e) {
        throw new CheckRunningFailedException("Interrupted while running check", e);
      } finally {
        runningChecks.remove(userId);
        submissionDurationTimer.record(Duration.between(start, Instant.now()));
        queueLengthCounter.decrementAndGet();
        process.toHandle().destroyForcibly();
      }

      if (check.getCompilationOutput() != null) {
        throw new CompilationFailedException(check.getCompilationOutput());
      }

      if (check.getError() != null) {
        throw new CheckRunningFailedException(check.getError());
      }

      return check.getResult().toSubmissionResult();
    }
  }

  // yea, this method is horrible. It is localized in this class though
  private BiConsumer<MessageClient, ProtocolMessage> getMessageConsumer() {
    return (messageClient, protocolMessage) -> {
      RunningCheck runningCheck = runningChecks.get(protocolMessage.getUid());

      // if you have already cleaned up after a message, ignore the rest
      // this should not happen
      if (runningCheck == null) {
        log.warn(
            "Got a message ({}) when I was already not running anything with uid '{}'",
            protocolMessage.getClassName(), protocolMessage.getUid()
        );
        return;
      }

      if (protocolMessage instanceof SlaveDiedWithUnknownError) {
        runningCheck.setError(((SlaveDiedWithUnknownError) protocolMessage).getMessage());
      } else if (protocolMessage instanceof SlaveTimedOut) {
        runningCheck.setError("Slave got no master response.");
      } else if (protocolMessage instanceof SubmissionResult) {
        SubmissionResult result = (SubmissionResult) protocolMessage;
        runningCheck.result.add(result.getFileName(), result.getResult());
      } else if (protocolMessage instanceof CompilationFailed) {
        runningCheck.setCompilationOutput(((CompilationFailed) protocolMessage).getOutput());
      } else if (protocolMessage instanceof SlaveComputationTookTooLong) {
        runningCheck.result.add("All", ImmutableCheckResult.builder()
            .check("Maximum computation time")
            .result(ResultType.FAILED)
            .message(
                "Your program did not finish in time. The following is my *best guess* at "
                    + "what your program successfully finished.\n"
                    + "The last check I attempted to run was "
                    + runningCheck.result.startedCheckContext
                    + "."
            )
            .errorOutput("")
            .build()
        );
        // Slave does not send a dying message in this case, as it can't anymore
        runningCheck.getLock().release();
      } else if (protocolMessage instanceof DyingMessage) {
        runningCheck.getLock().release();
      } else if (protocolMessage instanceof StartedCheck) {
        runningCheck.getResult()
            .setStartedCheckContext(((StartedCheck) protocolMessage).getCheckContext());
      }
    };
  }

  @Override
  public void afterPropertiesSet() {
    slaveManager = new SlaveManager(
        getMessageConsumer(),
        slaveClasspath,
        Duration.ofSeconds(maxComputationTimeSeconds)
    );
    slaveManager.start();
  }

  @Override
  public void destroy() {
    slaveManager.stop();
  }

  @Getter
  @Setter
  private static class RunningCheck {

    private Semaphore lock;
    private String error;
    private PartialResults result;
    private CompilationOutput compilationOutput;

    RunningCheck(Semaphore lock) {
      this.lock = lock;
      result = new PartialResults();
    }
  }

  /**
   * Contains partial results. The client reports back as soon as it enters or finishes a check.
   * This allows showing partial results to the user as well as localizing where their program
   * failed.
   */
  private static class PartialResults {

    private String startedCheckContext;
    private Map<String, List<CheckResult>> results;

    private PartialResults() {
      this.results = new HashMap<>();
    }

    /**
     * Adds the result for a single test.
     *
     * @param name the name of the test
     * @param result the result
     */
    void add(String name, CheckResult result) {
      List<CheckResult> list = results.getOrDefault(name, new ArrayList<>());
      list.add(result);
      results.put(name, list);
    }

    /**
     * Sets the name and some context for the started check.
     *
     * @param context the name and some context for the started check
     */
    void setStartedCheckContext(String context) {
      this.startedCheckContext = context;
    }

    ImmutableSubmissionCheckResult toSubmissionResult() {
      return ImmutableSubmissionCheckResult.builder()
          .putAllFileResults(results)
          .build();
    }
  }
}
