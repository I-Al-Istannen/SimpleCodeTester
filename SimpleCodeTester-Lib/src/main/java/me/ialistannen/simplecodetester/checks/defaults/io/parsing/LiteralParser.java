package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.LiteralIoMatcher;

/**
 * A parser for a {@link LiteralIoMatcher}.
 */
class LiteralParser implements Parser {

  @Override
  public boolean canParse(String s) {
    return s.startsWith("<l");
  }

  @Override
  public InterleavedIoMatcher parse(String s) {
    return new LiteralIoMatcher(s.substring(2));
  }
}
