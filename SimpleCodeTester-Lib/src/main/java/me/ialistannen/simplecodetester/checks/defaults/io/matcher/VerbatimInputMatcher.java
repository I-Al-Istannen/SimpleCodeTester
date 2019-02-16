package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import java.util.Objects;
import me.ialistannen.simplecodetester.checks.defaults.io.Block;

/**
 * A matcher that just matches against the input with no prefix or other functionality.
 */
public class VerbatimInputMatcher implements InterleavedIoMatcher {

  private String content;

  /**
   * Creates a new verbatim input matcher.
   *
   * @param content the content
   */
  public VerbatimInputMatcher(String content) {
    this.content = content;
  }

  @Override
  public boolean match(Block<String> output) {
    return output.next().equals(content);
  }

  @Override
  public String getError() {
    return "Expected '" + content + "'";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VerbatimInputMatcher that = (VerbatimInputMatcher) o;
    return Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(content);
  }

  @Override
  public String toString() {
    return content;
  }
}
