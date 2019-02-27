package me.ialistannen.simplecodetester.backend.services.checkrunning;

import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.Setter;
import me.ialistannen.simplecodetester.backend.db.entities.CodeCheck;
import me.ialistannen.simplecodetester.backend.exception.CheckAlreadyRunningException;
import me.ialistannen.simplecodetester.backend.exception.CheckRunningFailedException;
import me.ialistannen.simplecodetester.backend.exception.CompilationFailedException;
import me.ialistannen.simplecodetester.checks.CheckResult;
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
import me.ialistannen.simplecodetester.jvmcommunication.protocol.masterbound.SubmissionResult;
import me.ialistannen.simplecodetester.submission.Submission;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CheckRunnerService implements DisposableBean, InitializingBean {

  @Value("${runner.classpath}")
  private String[] slaveClasspath;
  @Value("${runner.max-computation-time-seconds}")
  private long maxComputationTimeSeconds;

  private SlaveManager slaveManager;

  private Map<String, RunningCheck> runningChecks;

  public CheckRunnerService() {
    this.runningChecks = new ConcurrentHashMap<>();
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
  public synchronized SubmissionCheckResult check(String userId, Submission submission,
      List<CodeCheck> checks) {
    // TODO: 30.01.19 Allow running in parallel?
    if (runningChecks.containsKey(userId)) {
      throw new CheckAlreadyRunningException();
    }

    Semaphore semaphore = new Semaphore(0);
    RunningCheck check = new RunningCheck(semaphore);
    runningChecks.put(userId, check);

    List<String> checksToRun = checks.stream()
        .map(CodeCheck::getText)
        .collect(toList());

    slaveManager.runSubmission(submission, checksToRun, userId);

    try {
      if (!semaphore.tryAcquire(maxComputationTimeSeconds + 20, TimeUnit.SECONDS)) {
        throw new CheckRunningFailedException(
            "Not sure, but I got no message. Maybe the slave died horribly?"
        );
      }
    } catch (InterruptedException e) {
      throw new CheckRunningFailedException("Interrupted while running check", e);
    } finally {
      runningChecks.remove(userId);
    }

    if (check.getCompilationOutput() != null) {
      throw new CompilationFailedException(check.getCompilationOutput());
    }

    if (check.getError() != null) {
      throw new CheckRunningFailedException(check.getError());
    }

    // TODO: 27.12.18 Slave killed due to timeout

    return check.getResult().toSubmissionResult();
  }


  private BiConsumer<MessageClient, ProtocolMessage> getMessageConsumer() {
    return (messageClient, protocolMessage) -> {
      RunningCheck runningCheck = runningChecks.get(protocolMessage.getUid());
      if (protocolMessage instanceof SlaveDiedWithUnknownError) {
        runningCheck.setError(((SlaveDiedWithUnknownError) protocolMessage).getMessage());
        runningCheck.getLock().release();
      } else if (protocolMessage instanceof SlaveTimedOut) {
        runningCheck.setError("Slave got no master response.");
        runningCheck.getLock().release();
      } else if (protocolMessage instanceof SubmissionResult) {
        SubmissionResult result = (SubmissionResult) protocolMessage;
        runningCheck.result.add(result.getFileName(), result.getResult());
//        runningCheck.getLock().release();
      } else if (protocolMessage instanceof CompilationFailed) {
        runningCheck.setCompilationOutput(((CompilationFailed) protocolMessage).getOutput());
        runningCheck.getLock().release();
      } else if (protocolMessage instanceof SlaveComputationTookTooLong) {
//        runningCheck.setError("Computation took too long!");
        runningCheck.getLock().release();
      } else if (protocolMessage instanceof DyingMessage) {
        runningCheck.getLock().release();
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

  private static class PartialResults {

    private Map<String, List<CheckResult>> results;

    private PartialResults() {
      this.results = new HashMap<>();
    }

    void add(String name, CheckResult result) {
      List<CheckResult> list = results.getOrDefault(name, new ArrayList<>());
      list.add(result);
      results.put(name, list);
    }

    ImmutableSubmissionCheckResult toSubmissionResult() {
      return ImmutableSubmissionCheckResult.builder()
          .putAllFileResults(results)
          .build();
    }
  }
}
