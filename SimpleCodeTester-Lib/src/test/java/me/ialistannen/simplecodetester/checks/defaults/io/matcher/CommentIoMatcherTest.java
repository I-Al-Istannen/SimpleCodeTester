package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import me.ialistannen.simplecodetester.checks.defaults.io.Block;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommentIoMatcherTest {

  @ParameterizedTest(name = "\"{0}\" is a comment: {1}")
  @CsvSource({
      "Test, true",
      "Error, true",
      "Magic, true",
      "'<>Error,', true",
      "'<> Error,magic', true",
      "<> Error, true",
      "<>Error something, true",
      "'', true"
  })
  void testErrorMatches(String input, boolean match) {
    String correctInput = input.replace("<>", "#");

    CommentIoMatcher matcher = new CommentIoMatcher(correctInput);
    assertEquals(
        match,
        matcher.match(new Block<>(Collections.singletonList(correctInput)))
    );
  }


}