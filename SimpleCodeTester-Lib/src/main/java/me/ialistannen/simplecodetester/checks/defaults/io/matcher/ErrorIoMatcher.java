package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import me.ialistannen.simplecodetester.checks.defaults.io.Block;

/**
 * A matcher matching error messages.
 */
public class ErrorIoMatcher implements InterleavedIoMatcher {

  private transient RegularExpressionIoMatcher matcher;

  /**
   * Creates a new error io matcher.
   */
  public ErrorIoMatcher() {
    this.matcher = new RegularExpressionIoMatcher("Error, .*");
  }

  @Override
  public boolean match(Block<String> output) {
    return matcher.match(output);
  }

  @Override
  public String getError() {
    return "Expected an error message";
  }

  @Override
  public String toString() {
    return "<e";
  }
}
