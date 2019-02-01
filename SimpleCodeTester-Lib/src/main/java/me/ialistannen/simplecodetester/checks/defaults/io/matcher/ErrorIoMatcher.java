package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

/**
 * A matcher matching error messages.
 */
public class ErrorIoMatcher implements InterleavedIoMatcher {

  private transient RegularExpressionIoMatcher matcher;

  /**
   * Creates a new error io matcher.
   */
  public ErrorIoMatcher() {
    this.matcher = new RegularExpressionIoMatcher("Error.*");
  }

  @Override
  public boolean match(String line) {
    return matcher.match(line);
  }

  @Override
  public String getError() {
    return "Expected an error message";
  }
}
