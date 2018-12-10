package me.ialistannen.simplecodetester.checks;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import me.ialistannen.simplecodetester.checks.ImmutableSubmissionCheckResult.Builder;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;

public class CheckRunner {

  private List<Check> checks;

  /**
   * Creates a new CheckRunner that will check a {@link CompiledSubmission}.
   *
   * @param checks the {@link Check}s to use
   */
  public CheckRunner(List<Check> checks) {
    this.checks = new ArrayList<>(checks);
  }

  /**
   * Adds a new {@link Check} to this runner
   *
   * @param check the {@link Check} to add
   */
  public void addCheck(Check check) {
    checks.add(check);
  }

  /**
   * Runs all checks against the given {@link CompiledSubmission}.
   *
   * @param compiledSubmission the {@link CompiledSubmission} to test
   * @return the {@link SubmissionCheckResult}
   */
  public SubmissionCheckResult checkSubmission(CompiledSubmission compiledSubmission) {
    Builder builder = ImmutableSubmissionCheckResult.builder();

    for (CompiledFile file : compiledSubmission.files()) {
      List<CheckResult> checkResults = checks.stream()
          .map(check -> tryCheck(check, file))
          .collect(toList());
      builder.putFileResults(file, checkResults);
    }

    return builder.build();
  }

  private CheckResult tryCheck(Check check, CompiledFile file) {
    try {
      return check.check(file);
    } catch (CheckFailedException e) {
      return ImmutableCheckResult.builder()
          .message(e.getMessage())
          .check(check)
          .successful(false)
          .build();
    }
  }
}
