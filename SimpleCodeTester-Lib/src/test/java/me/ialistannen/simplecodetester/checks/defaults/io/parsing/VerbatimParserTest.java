package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import me.ialistannen.simplecodetester.checks.defaults.io.matcher.VerbatimInputMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VerbatimParserTest {

  private VerbatimParser verbatimParser;

  @BeforeEach
  void setUp() {
    verbatimParser = new VerbatimParser();
  }

  @ParameterizedTest(name = "\"{0}\" can be read verbatim: {1}")
  @CsvSource({
      "<<e, false",
      "hello, true",
      "Error, true",
      "<l, false",
      "<l  , false",
      "> cool stuff, false",
      "cool stuff, true"
  })
  void canParseVerbatim(String input, boolean canParse) {
    assertEquals(
        canParse,
        verbatimParser.canParse(input)
    );
  }

  @ParameterizedTest(name = "Parsing \"{0}\"")
  @CsvSource({
      "Cool stuff",
      "Is here",
      "magic with spaces",
      "and with \" some special",
      "'characters in there\t\nHey"
  })
  void parsesToCorrectVerbatim(String input) {
    assertEquals(
        new VerbatimInputMatcher(input),
        verbatimParser.parse(input)
    );
  }

}