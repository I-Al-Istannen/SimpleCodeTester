package me.ialistannen.simplecodetester.backend.endpoints.checks.parsers;

import me.ialistannen.simplecodetester.checks.Check;

/**
 * A parser for a check.
 *
 * @param <T> the type of the check
 */
public interface CheckParser<T extends Check> {

  /**
   * Parses the given input to a check.
   *
   * @param payload the input payload
   * @return the parsed result
   */
  T parse(String payload);
}
