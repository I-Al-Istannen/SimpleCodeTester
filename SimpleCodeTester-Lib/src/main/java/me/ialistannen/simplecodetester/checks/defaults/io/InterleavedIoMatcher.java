package me.ialistannen.simplecodetester.checks.defaults.io;

/**
 * An output matcher type.
 */
public interface InterleavedIoMatcher {

  /**
   * Checks whether the line matches and returns an error if not.
   *
   * @param line the line to match
   * @return true if it matches the line
   */
  boolean match(String line);

  /**
   * Returns the error message.
   *
   * @return the error message
   */
  String getError();
}
