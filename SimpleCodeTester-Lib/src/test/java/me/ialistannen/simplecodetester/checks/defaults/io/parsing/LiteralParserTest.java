package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.LiteralIoMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LiteralParserTest {

  private LiteralParser literalParser;

  @BeforeEach
  void setUp() {
    literalParser = new LiteralParser();
  }

  @ParameterizedTest(name = "\"{0}\" is a literal: {1}")
  @CsvSource({
      "<<e, false",
      "hello, false",
      "Error, false",
      "<l, true",
      "<l  , true"
  })
  void canParseLiteral(String input, boolean canParse) {
    assertEquals(
        canParse,
        literalParser.canParse(input)
    );
  }

  @ParameterizedTest(name = "Parsing \"{0}\"")
  @CsvSource({
      "<l",
      "<l  ",
      "<lSome test",
      "<l<e",
      "<l<lNested literal not parsed",
  })
  void parsesToCorrectLiteral(String input) {
    assertEquals(
        new LiteralIoMatcher(input.substring(2)),
        literalParser.parse(input)
    );
  }

}