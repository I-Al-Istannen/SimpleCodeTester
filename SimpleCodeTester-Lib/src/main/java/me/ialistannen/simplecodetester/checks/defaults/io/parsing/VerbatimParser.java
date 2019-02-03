package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.VerbatimInputMatcher;

/**
 * A parser for the {@link VerbatimInputMatcher}.
 */
class VerbatimParser implements Parser {

  @Override
  public boolean canParse(String input) {
    return !input.startsWith(">") && !input.startsWith("<");
  }

  @Override
  public InterleavedIoMatcher parse(String input) {
    return new VerbatimInputMatcher(input);
  }
}
