package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ErrorParserTest {

  @ParameterizedTest(name = "\"{0}\" is an error: {1}")
  @CsvSource({
      "<<e, false",
      "hello, false",
      "Error, false",
      "<e, true",
      "<e  , true"
  })
  void canParseError(String input, boolean canParse) {
    assertEquals(
        canParse,
        new ErrorParser().canParse(input)
    );
  }
}