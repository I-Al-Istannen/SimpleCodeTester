package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

/**
 * An {@link InterleavedIoMatcher} that matches a given literal
 */
public class LiteralIoMatcher implements InterleavedIoMatcher {

  private final String literal;

  /**
   * Creates a new literal io matcher.
   *
   * @param literal the literal to expect
   */
  public LiteralIoMatcher(String literal) {
    this.literal = literal;
  }

  @Override
  public boolean match(String line) {
    return literal.equals(line);
  }

  @Override
  public String getError() {
    return "Expected '" + literal + "'";
  }
}
