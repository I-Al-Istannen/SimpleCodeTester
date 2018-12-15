package me.ialistannen.simplecodetester.checks;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * The result of executing a single {@link Check}.
 */
@Value.Immutable
@Gson.TypeAdapters
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
        .build();
  }
}
