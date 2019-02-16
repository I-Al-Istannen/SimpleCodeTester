package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import me.ialistannen.simplecodetester.checks.defaults.io.Block;

/**
 * An output matcher type.
 */
public interface InterleavedIoMatcher {

  /**
   * Checks whether the line matches and returns an error if not.
   *
   * @param output the output block
   * @return true if it matches the line
   */
  boolean match(Block<String> output);

  /**
   * Returns the error message.
   *
   * @return the error message
   */
  String getError();
}
