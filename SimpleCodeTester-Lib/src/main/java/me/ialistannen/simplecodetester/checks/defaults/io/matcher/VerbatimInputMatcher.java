package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

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
  public boolean match(String line) {
    return line.equals(content);
  }

  @Override
  public String getError() {
    return "Expected '" + content + "'";
  }

  @Override
  public String toString() {
    return content;
  }
}
