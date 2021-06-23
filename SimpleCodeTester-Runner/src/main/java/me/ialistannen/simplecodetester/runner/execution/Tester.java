package me.ialistannen.simplecodetester.runner.execution;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.compilation.ImmutableCompilationOutput;
import me.ialistannen.simplecodetester.result.ImmutableResult;
import me.ialistannen.simplecodetester.result.ImmutableTimeoutData;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.runner.util.ProgramResult;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import me.ialistannen.simplecodetester.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tester is responsible for starting and stopping a test container as well as passing along input
 * and reading the results.
 */
public class Tester {

  private static final Logger LOGGER = LoggerFactory.getLogger(Tester.class);

  private final List<String> startCommand;
  private final List<String> killCommand;
  private final ProgramExecutor programExecutor;
  private final Duration maxRuntime;
  private final Gson gson;

  public Tester(List<String> startCommand, List<String> killCommand, Duration maxRuntime,
      Gson gson) {
    this.startCommand = List.of(startCommand.toArray(String[]::new));
    this.killCommand = List.of(killCommand.toArray(String[]::new));
    this.maxRuntime = maxRuntime;
    this.gson = gson;

    this.programExecutor = new ProgramExecutor();
  }

  /**
   * Tests a single submission.
   *
   * @param completeTask the task to test
   * @return the result
   */
  public synchronized Result test(CompleteTask completeTask) {
    StreamsProcessOutput<ProgramResult> task = start(completeTask);

    boolean killed = false;
    try {
      task.get(maxRuntime.getSeconds(), TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.warn("Testing failed", e);
    } catch (TimeoutException e) {
      forceKill(completeTask.userId());
      killed = true;
    }

    String lastCheck = "Unknown";
    Map<String, List<CheckResult>> results = new HashMap<>();
    List<String> stdOut = task.getCurrentStdOut().lines().collect(Collectors.toList());

    for (int i = 1; i < stdOut.size(); i++) {
      String line = stdOut.get(i);
      try {
        lastCheck = parseResultLine(line, results);
      } catch (Exception e) {
        LOGGER.warn("Received invalid JSON (" + completeTask.userId() + ")", e);
      }
    }

    if (!task.getCurrentStdErr().isBlank()) {
      LOGGER.warn("Received STDERR for ({}): {}", completeTask.userId(), task.getCurrentStdErr());
    }

    return ImmutableResult.builder()
        .timeoutData(
            killed
                ? Optional.of(ImmutableTimeoutData.builder().lastTest(lastCheck).build())
                : Optional.empty()
        )
        .putAllFileResults(results)
        .compilationOutput(parseCompilationOutput(stdOut, completeTask.userId()))
        .build();
  }

  private CompilationOutput parseCompilationOutput(List<String> lines, String userId) {
    if (lines.isEmpty()) {
      LOGGER.warn("Received no compilation output ({})", userId);
      return ImmutableCompilationOutput.builder()
          .diagnostics(Map.of())
          .successful(false)
          .output("Received no compilation output")
          .files(List.of())
          .build();
    }
    try {
      return gson.fromJson(lines.get(0), CompilationOutput.class);
    } catch (JsonSyntaxException e) {
      LOGGER.warn("Received invalid json (" + userId + ")", e);
      return ImmutableCompilationOutput.builder()
          .diagnostics(Map.of())
          .successful(false)
          .output(ExceptionUtil.getStacktrace(e))
          .files(List.of())
          .build();
    }
  }

  private String parseResultLine(String line, Map<String, List<CheckResult>> resultList) {
    Map<String, JsonElement> data = gson.fromJson(
        line,
        new TypeToken<Map<String, JsonElement>>() {
        }.getType()
    );
    if (data.containsKey("is-check-start")) {
      return data.get("check-name").getAsString();
    } else {
      CheckResult singleResult = gson.fromJson(data.get("data"), CheckResult.class);
      String filename = data.get("file-name").getAsString();
      List<CheckResult> results = resultList.getOrDefault(filename, new ArrayList<>());
      results.add(singleResult);

      resultList.put(filename, results);

      return singleResult.check();
    }
  }

  private StreamsProcessOutput<ProgramResult> start(CompleteTask taskToSend) {
    List<String> command = generateFullCommand(startCommand, taskToSend.userId());
    String stdin = gson.toJson(taskToSend);
    return programExecutor.execute(command, stdin);
  }

  private void forceKill(String userId) {
    LOGGER.info("Force killing process ({})", userId);
    programExecutor.execute(generateFullCommand(killCommand, userId));
  }

  private List<String> generateFullCommand(List<String> base, String userId) {
    List<String> commands = new ArrayList<>(base);
    commands.add(userId);

    return commands;
  }
}
