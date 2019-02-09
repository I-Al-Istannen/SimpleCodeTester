package me.ialistannen.simplecodetester.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import me.ialistannen.simplecodetester.checks.defaults.StaticInputOutputCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.ErrorIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.LiteralIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.RegularExpressionIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.VerbatimInputMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConfiguredGsonTest {

  private Gson gson;

  @BeforeEach
  void setup() {
    gson = ConfiguredGson.createGson();
  }

  @Test
  void canHandlePaths() {
    Path path = Paths.get("/randomPath");

    assertEquals(
        path,
        gson.fromJson(gson.toJson(path, Path.class), Path.class)
    );
  }

  @Test
  void canHandleStaticInputOutputMatcher() {
    StaticInputOutputCheck check = new StaticInputOutputCheck(
        List.of("some", "input"),
        "THIS IS EXPECTED",
        "NiceCheck"
    );
    assertEquals(
        check,
        gson.fromJson(gson.toJson(check), StaticInputOutputCheck.class)
    );
  }

  @ParameterizedTest
  @MethodSource("generateIoMatcher")
  void canHandleIoMatcher(InterleavedIoMatcher matcher) {
    assertEquals(
        matcher,
        gson.fromJson(gson.toJson(matcher, InterleavedIoMatcher.class), InterleavedIoMatcher.class)
    );
  }

  private static Stream<Arguments> generateIoMatcher() {
    return Stream.of(
        Arguments.of(new LiteralIoMatcher("hey")),
        Arguments.of(new VerbatimInputMatcher("hello")),
        Arguments.of(new RegularExpressionIoMatcher(".+"))
    );
  }

  @Test
  void canHandleErrorIoMatcher() {
    String json = gson.toJson(new ErrorIoMatcher(), InterleavedIoMatcher.class);

    InterleavedIoMatcher fromJson = gson.fromJson(json, InterleavedIoMatcher.class);

    assertTrue(
        fromJson instanceof ErrorIoMatcher,
        "Was no error matcher"
    );
  }
}