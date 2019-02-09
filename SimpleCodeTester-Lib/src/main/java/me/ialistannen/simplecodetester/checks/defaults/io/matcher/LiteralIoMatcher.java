package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LiteralIoMatcher that = (LiteralIoMatcher) o;
    return Objects.equals(literal, that.literal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(literal);
  }

  @Override
  public String toString() {
    return "<l" + literal;
  }
}
