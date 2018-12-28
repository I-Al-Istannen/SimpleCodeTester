package me.ialistannen.simplecodetester.checks;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

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
  public abstract boolean successful();

  /**
   * The associated message, if any.
   *
   * @return associated message, if any. An empty string if none
   */
  public abstract String message();

  /**
   * The {@link System#err} output.
   *
   * @return the {@link System#err} output
   */
  public abstract String errorOutput();

  /**
   * Returns a successful CheckResult with no message.
   *
   * @param check the check that returned this
   * @return a successful CheckResult with no message.
   */
  public static CheckResult emptySuccess(Check check) {
    return ImmutableCheckResult.builder()
        .successful(true)
        .check(check.name())
        .message("")
        .errorOutput("")
        .build();
  }

  /**
   * Returns a CheckResult for a failure using the given message.
   *
   * @param message the message to use
   * @return the resulting CheckResult
   */
  public static CheckResult failure(String message) {
    return ImmutableCheckResult.builder()
        .successful(false)
        .message(message)
        .errorOutput("")
        .build();
  }
}
