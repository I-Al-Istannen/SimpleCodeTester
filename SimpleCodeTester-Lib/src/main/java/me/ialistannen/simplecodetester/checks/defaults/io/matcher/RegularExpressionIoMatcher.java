package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import java.util.Objects;
import me.ialistannen.simplecodetester.checks.defaults.io.Block;

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
  public boolean match(Block<String> output) {
    return output.next().matches(pattern);
  }

  @Override
  public String getError() {
    return "Expected a match for pattern '" + pattern + "'";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RegularExpressionIoMatcher that = (RegularExpressionIoMatcher) o;
    return Objects.equals(pattern, that.pattern);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pattern);
  }

  @Override
  public String toString() {
    return "<r" + pattern;
  }
}
