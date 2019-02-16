package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import me.ialistannen.simplecodetester.checks.defaults.io.Block;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LiteralIoMatcherTest {

  @ParameterizedTest(name = "\"{1}\" matches \"{0}\" : {2}")
  @CsvSource({
      "<eError, Error, false",
      "<eError, <eError, true",
      "λ, λ, true",
      "' ', ' ', true"
  })
  void testLiteralMatcher(String matcherText, String input, boolean match) {
    LiteralIoMatcher matcher = new LiteralIoMatcher(matcherText);

    assertEquals(
        match,
        matcher.match(new Block<>(Collections.singletonList(input)))
    );
  }
}