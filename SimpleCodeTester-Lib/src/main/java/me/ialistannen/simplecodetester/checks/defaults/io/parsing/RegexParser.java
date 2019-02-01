package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.RegularExpressionIoMatcher;

/**
 * A {@link Parser} that creates a {@link RegularExpressionIoMatcher}.
 */
class RegexParser implements Parser {

  @Override
  public boolean canParse(String input) {
    return input.startsWith("<r");
  }

  @Override
  public InterleavedIoMatcher parse(String input) {
    return new RegularExpressionIoMatcher(input.substring(2));
  }
}
