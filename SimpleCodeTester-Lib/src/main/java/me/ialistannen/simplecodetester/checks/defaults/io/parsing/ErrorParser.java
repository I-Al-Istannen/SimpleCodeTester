package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.ErrorIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;

/**
 * A {@link Parser} for an {@link ErrorIoMatcher}.
 */
class ErrorParser implements Parser {

  @Override
  public boolean canParse(String input) {
    return input.startsWith("<e");
  }

  @Override
  public InterleavedIoMatcher parse(String input) {
    return new ErrorIoMatcher();
  }
}
