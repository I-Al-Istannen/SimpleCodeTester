package me.ialistannen.simplecodetester.checks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import org.immutables.value.Value;

@Value.Immutable
public abstract class SubmissionCheckResult {

  /**
   * Returns the {@link CheckResult}s for each {@link CompiledFile}.
   *
   * @return the check results for each file
   */
  public abstract Map<CompiledFile, List<CheckResult>> fileResults();

  /**
   * Checks whether all tests succeeded.
   *
   * @return true if all <em>tests</em> succeeded
   */
  public boolean overallSuccessful() {
    return fileResults().values().stream()
        .flatMap(Collection::stream)
        .allMatch(CheckResult::successful);
  }
}
