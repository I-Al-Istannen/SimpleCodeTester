package me.ialistannen.simplecodetester.checks;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Optional;
import me.ialistannen.simplecodetester.checks.Check.CheckFile;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult;
import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

/**
 * The result of executing a single {@link Check}.
 */
@Value.Immutable
@Gson.TypeAdapters
@JsonSerialize
public abstract class CheckResult {

  /**
   * The name of the check that was used to check the file.
   *
   * @return the name of the used check
   */
  public abstract String check();

  /**
   * Checks whether the check was successful.
   *
   * @return true if the check was successful
   */
  public abstract ResultType result();

  /**
   * The associated message, if any.
   *
   * @return associated message, if any. An empty string if none
   */
  public abstract String message();

  /**
   * Returns the in and output of of the check, maybe with error messages interleaved.
   *
   * @return the in and output of of the check, maybe with error messages interleaved.
   */
  public abstract List<LineResult> output();

  /**
   * The {@link System#err} output.
   *
   * @return the {@link System#err} output
   */
  public abstract String errorOutput();

  /**
   * @return a list with all files
   */
  @Default
  public List<CheckFile> files() {
    return List.of();
  }

  /**
   * @return the time in milliseconds executing this test took, if measured
   */
  @Default
  @Value.Auxiliary
  public Optional<Long> durationMillis() {
    return Optional.empty();
  }

  /**
   * Returns a successful CheckResult with no message.
   *
   * @param check the check that returned this
   * @return a successful CheckResult with no message
   */
  public static CheckResult emptySuccess(Check check) {
    return ImmutableCheckResult.builder()
        .result(ResultType.SUCCESSFUL)
        .check(check.name())
        .message("")
        .errorOutput("")
        .build();
  }

  /**
   * Returns a CheckResult with no message and the type {@link ResultType#NOT_APPLICABLE}.
   *
   * @param check the check that returned this
   * @return a CheckResult with no message and {@link ResultType#NOT_APPLICABLE}
   */
  public static CheckResult notApplicable(Check check) {
    return ImmutableCheckResult.builder()
        .result(ResultType.NOT_APPLICABLE)
        .check(check.name())
        .message("")
        .errorOutput("")
        .build();
  }

  /**
   * The result type of a check.
   */
  public enum ResultType {
    /**
     * The check was successful.
     */
    SUCCESSFUL,
    /**
     * The check failed.
     */
    FAILED,
    /**
     * The check was not applicable to the file.
     */
    NOT_APPLICABLE
  }
}
