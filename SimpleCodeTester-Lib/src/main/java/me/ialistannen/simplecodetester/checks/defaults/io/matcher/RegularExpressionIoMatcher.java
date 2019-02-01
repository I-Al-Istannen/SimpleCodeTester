package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

/**
 * Matches a line using a regular expression.
 */
public class RegularExpressionIoMatcher implements InterleavedIoMatcher {

  private String pattern;

  /**
   * The pattern to match against.
   *
   * @param pattern the pattern to match
   */
  public RegularExpressionIoMatcher(String pattern) {
    this.pattern = pattern;
  }

  @Override
  public boolean match(String line) {
    return line.matches(pattern);
  }

  @Override
  public String getError() {
    return "Expected a match for pattern '" + pattern + "'";
  }
}
