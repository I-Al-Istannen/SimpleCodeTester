package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommentParserTest {

  @ParameterizedTest(name = "\"{0}\" is a comment: {1}")
  @CsvSource({
      "<> Jup, true",
      "<>, true",
      "hello, false",
      "<e, false"
  })
  void canParseComment(String input, boolean canParse) {
    assertEquals(
        canParse,
        new CommentParser().canParse(input.replace("<>", "#"))
    );
  }
}