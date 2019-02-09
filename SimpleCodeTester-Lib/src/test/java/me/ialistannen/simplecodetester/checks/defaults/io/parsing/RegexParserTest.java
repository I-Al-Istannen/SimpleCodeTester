package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.RegularExpressionIoMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RegexParserTest {

  private RegexParser regexParser;

  @BeforeEach
  void setUp() {
    regexParser = new RegexParser();
  }

  @ParameterizedTest(name = "\"{0}\" is a literal: {1}")
  @CsvSource({
      "<<e, false",
      "hello, false",
      "Error, false",
      "<r, true",
      "<r  , true",
      "<r.+  , true"
  })
  void canParseRegex(String input, boolean canParse) {
    assertEquals(
        canParse,
        regexParser.canParse(input)
    );
  }

  @ParameterizedTest(name = "Parsing \"{0}\"")
  @CsvSource({
      "<r.+",
      "<r[a-z]+  ",
      "<r(a,[a-z]{1,5})",
  })
  void parsesToCorrectRegex(String input) {
    assertEquals(
        new RegularExpressionIoMatcher(input.substring(2)),
        regexParser.parse(input)
    );
  }

}