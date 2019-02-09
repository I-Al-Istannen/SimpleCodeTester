package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VerbatimInputMatcherTest {

  @ParameterizedTest(name = "\"{1}\" matches \"{0}\" : {2}")
  @CsvSource({
      "<eError, Error, false",
      "<eError, <eError, true",
      "λ, λ, true",
      "' ', ' ', true"
  })
  void testVerbatimMatcher(String matcherText, String input, boolean match) {
    VerbatimInputMatcher matcher = new VerbatimInputMatcher(matcherText);

    assertEquals(
        match,
        matcher.match(input)
    );
  }

}