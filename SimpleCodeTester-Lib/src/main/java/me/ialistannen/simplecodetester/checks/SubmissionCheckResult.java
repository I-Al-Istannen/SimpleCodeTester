package me.ialistannen.simplecodetester.checks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.ialistannen.simplecodetester.checks.CheckResult.ResultType;
import me.ialistannen.simplecodetester.submission.Submission;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * The result of checking a whole {@link Submission}.
 */
@Gson.TypeAdapters
@Value.Immutable
public abstract class SubmissionCheckResult {

  /**
   * Returns the {@link CheckResult}s for each file.
   *
   * @return the check results for each file
   */
  public abstract Map<String, List<CheckResult>> fileResults();

  /**
   * Checks whether all tests succeeded.
   *
   * @return true if all <em>tests</em> succeeded
   */
  public boolean overallSuccessful() {
    return fileResults().values().stream()
        .flatMap(Collection::stream)
        .allMatch(checkResult -> checkResult.result() != ResultType.FAILED);
  }
}
