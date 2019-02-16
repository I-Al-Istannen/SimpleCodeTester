package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import me.ialistannen.simplecodetester.checks.defaults.io.Block;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RegularExpressionIoMatcherTest {

  @ParameterizedTest(name = "\"{1}\" matches \"{1}\" : {2}")
  @CsvSource({
      ".+, hello, true",
      "' ', ' ', true",
      "\\d+, test, false",
      "\\d+, 222323, true",
      "a+c, aaaaaaca, false", // anchored
      "a+ca, aaaaaaca, true" // anchored
  })
  void testRegularMatcher(String matcherText, String input, boolean matches) {
    RegularExpressionIoMatcher matcher = new RegularExpressionIoMatcher(matcherText);

    assertEquals(
        matches,
        matcher.match(new Block<>(Collections.singletonList(input)))
    );
  }

}