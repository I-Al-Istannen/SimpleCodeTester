package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.CommentIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;

/**
 * A parser for comments
 */
class CommentParser implements Parser {

  @Override
  public boolean canParse(String input) {
    return input.startsWith("#");
  }

  @Override
  public InterleavedIoMatcher parse(String input) {
    return new CommentIoMatcher(input.substring(1));
  }
}
