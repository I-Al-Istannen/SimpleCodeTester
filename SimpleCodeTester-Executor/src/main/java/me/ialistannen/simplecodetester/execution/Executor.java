package me.ialistannen.simplecodetester.execution;

import static java.util.stream.Collectors.toCollection;

import com.google.gson.Gson;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.storage.CheckSerializer;
import me.ialistannen.simplecodetester.execution.compilation.Compiler;
import me.ialistannen.simplecodetester.execution.compilation.memory.Java11InMemoryCompiler;
import me.ialistannen.simplecodetester.execution.running.CheckRunner;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import me.ialistannen.simplecodetester.submission.Submission;

/**
 * The main driver class. An executor reads the task from a given reader, compiles it, runs it and
 * writes the results to the passed out stream.
 */
public class Executor {

  private final Gson gson;
  private final CheckSerializer checkSerializer;
  private final PrintStream out;

  public Executor(PrintStream out, Gson gson) {
    this.out = out;
    this.gson = gson;
    this.checkSerializer = new CheckSerializer(gson);
  }

  /**
   * Runs the given task.
   *
   * @param task the task to run
   */
  public void runTests(CompleteTask task) {
    CompiledSubmission submission = compile(task.submission());
    List<Check> checks = extractChecks(task);
    test(checks, submission);
  }

  private CompiledSubmission compile(Submission submission) {
    Compiler compiler = new Java11InMemoryCompiler();
    return compiler.compileSubmission(submission);
  }

  private List<Check> extractChecks(CompleteTask task) {
    return task.checks().stream()
        .map(checkSerializer::fromJson)
        .collect(toCollection(ArrayList::new));
  }

  private void test(List<Check> checks, CompiledSubmission submission) {
    CheckRunner checkRunner = new CheckRunner(checks);
    checkRunner.checkSubmission(submission, this::onCheckResult, this::onCheckStarted);
  }

  private void onCheckResult(String name, CheckResult result) {
    out.println(gson.toJson(Map.of("check-name", name, "data", result)));
  }

  private void onCheckStarted(String name) {
    out.println(gson.toJson(Map.of("check-name", name, "is-check-start", true)));
  }
}
