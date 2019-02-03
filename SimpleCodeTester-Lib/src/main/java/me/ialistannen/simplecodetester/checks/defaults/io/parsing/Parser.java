package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;

/**
 * A parser for an {@link InterleavedIoMatcher}.
 */
interface Parser {

  /**
   * Returns whether the parser can parse the given input
   *
   * @param input the input
   * @return true if the parser can parse the given input
   */
  boolean canParse(String input);

  /**
   * Parses the given input.
   *
   * @param input the input
   * @return the parsed matcher
   */
  InterleavedIoMatcher parse(String input);
}
