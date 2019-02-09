package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ErrorIoMatcherTest {

  private ErrorIoMatcher matcher;

  @BeforeEach
  void setup() {
    matcher = new ErrorIoMatcher();
  }

  @ParameterizedTest(name = "\"{0}\" is an error: {1}")
  @CsvSource({
      "Test, false",
      "Error, false",
      "Magic, false",
      "'Error,', false",
      "'Error,magic', false",
      "'Error, ', true",
      "'Error, something', true",
      "'Error, true!', true",
  })
  void testErrorMatches(String input, boolean match) {
    assertEquals(
        match,
        matcher.match(input)
    );
  }


}