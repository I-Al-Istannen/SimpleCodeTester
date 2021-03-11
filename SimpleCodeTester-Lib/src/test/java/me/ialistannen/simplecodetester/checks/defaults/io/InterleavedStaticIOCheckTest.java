package me.ialistannen.simplecodetester.checks.defaults.io;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.kit.informatik.Terminal;
import java.util.Collections;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.LiteralIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.parsing.InterleavedIoParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class InterleavedStaticIOCheckTest {

  @AfterEach
  void reset() {
    Terminal.reset();
  }

  @Test
  void matchingOutputHasNoError() {
    InterleavedStaticIOCheck check = getCheck(
        "> Hello", "<e", "> quit"
    );

    List<LineResult> result = check.getOutput(List.of(
        List.of(), // nothing before first read
        List.of("Error, wrong input")
    ));

    assertHasOrder(result, Type.INPUT, Type.OUTPUT, Type.INPUT);
  }

  @Test
  void matchingOutputHasNoErrorFromParser() {
    InterleavedStaticIOCheck check = getCheck(
        "> hello", "You there", "How are you", "> my dear", "friend", "> quit"
    );
    List<LineResult> result = check.getOutput(
        List.of(
            List.of(),
            List.of("You there", "How are you"),
            List.of("friend")
        )
    );
    assertHasOrder(
        result, Type.INPUT, Type.OUTPUT, Type.OUTPUT, Type.INPUT, Type.OUTPUT, Type.INPUT
    );
  }

  @Test
  void matchingOutputHasWrongOutput() {
    InterleavedStaticIOCheck check = getCheck(
        "> Hello", "<e", "> quit"
    );

    List<LineResult> result = check.getOutput(List.of(
        List.of(), // nothing before first read
        List.of("Correct input")
    ));

    assertHasOrder(result, Type.INPUT, Type.OUTPUT, Type.ERROR, Type.INPUT);
  }

  @Test
  void fusedOutput() {
    InterleavedStaticIOCheck check1 = getCheck(
        "a", "b", "c",
        "> a", "a", "b",
        "> b", "a",
        "> c"
    );
    List<LineResult> result = check1.getOutput(List.of(
        List.of("a"),
        List.of("a", "b"),
        List.of("a", "b", "c")
    ));

    assertHasOrder(
        result,
        Type.OUTPUT, Type.ERROR, Type.ERROR,
        Type.INPUT,
        Type.OUTPUT, Type.OUTPUT,
        Type.INPUT,
        Type.OUTPUT, Type.OUTPUT, Type.ERROR, Type.OUTPUT, Type.ERROR,
        Type.INPUT
    );
  }

  @Test
  void toStringIsInterleaved() {
    InterleavedStaticIOCheck check = getCheck(
        "> Hello", "World",
        "> Foo", "Bar",
        "> Regex", "<r.+",
        "> Error", "<e",
        "> quit"
    );

    assertEquals(
        "> Hello\nWorld\n> Foo\nBar\n> Regex\n<r.+\n> Error\n<e\n> quit",
        check.toString()
    );
  }

  @Test
  void toStringLeftoverInput() {
    InterleavedStaticIOCheck check = new InterleavedStaticIOCheck(
        List.of("Hello", "World!"), Collections.emptyList(), Collections.emptyList(), "Test"
    );

    assertEquals(
        "> Hello\n> World!",
        check.toString()
    );
  }

  @Test
  void tooMuchOutput() {
    InterleavedStaticIOCheck check = getCheck(
        "> Hello", "World", "> quit"
    );

    List<LineResult> output = check.getOutput(List.of(
        List.of(),
        List.of("World", "And stuff"),
        List.of("More leftover")
    ));

    assertEquals(
        List.of(
            new LineResult(Type.INPUT, "Hello"),
            new LineResult(Type.OUTPUT, "World"),
            new LineResult(Type.OUTPUT, "And stuff"),
            new LineResult(Type.ERROR, "Did not expect any output."),
            new LineResult(Type.INPUT, "quit"),
            new LineResult(Type.OUTPUT, "More leftover"),
            new LineResult(Type.ERROR, "Did not expect any output.")
        ),
        output
    );
  }

  @Test
  void outputWhereNotExpected() {
    InterleavedStaticIOCheck check = getCheck(
        "> Hello", "> You there", "> quit"
    );

    List<LineResult> output = check.getOutput(List.of(
        List.of(),
        List.of("World", "And stuff"),
        List.of("More leftover")
    ));

    assertEquals(
        List.of(
            new LineResult(Type.INPUT, "Hello"),
            new LineResult(Type.OUTPUT, "World"),
            new LineResult(Type.ERROR, "Did not expect any output."),
            new LineResult(Type.OUTPUT, "And stuff"),
            new LineResult(Type.ERROR, "Did not expect any output."),
            new LineResult(Type.INPUT, "You there"),
            new LineResult(Type.OUTPUT, "More leftover"),
            new LineResult(Type.ERROR, "Did not expect any output."),
            new LineResult(Type.INPUT, "quit")
        ),
        output
    );
  }

  @Test
  void leftoverMatchers() {
    InterleavedStaticIOCheck check = new InterleavedStaticIOCheck(
        List.of(),
        List.of(),
        List.of(List.of(new LiteralIoMatcher("World")), List.of(new LiteralIoMatcher("Bar"))),
        "Test"
    );

    List<LineResult> output = check.getOutput(List.of(
        List.of()
    ));

    assertEquals(
        List.of(
            new LineResult(Type.ERROR, "Expected 'World'"),
            new LineResult(Type.ERROR, "Expected 'Bar'")
        ),
        output
    );
  }

  @Test
  void leftoverInput() {
    InterleavedStaticIOCheck check = new InterleavedStaticIOCheck(
        List.of("Hello", "You there"), List.of(), List.of(), "Test"
    );

    List<LineResult> output = check.getOutput(List.of(
        List.of()
    ));

    assertEquals(
        List.of(
            new LineResult(Type.INPUT, "Hello"),
            new LineResult(Type.INPUT, "You there")
        ),
        output
    );
  }

  private void assertHasOrder(List<LineResult> results, LineResult.Type... expected) {
    assertThat(
        results.stream().map(LineResult::getType).toArray(LineResult.Type[]::new),
        is(expected)
    );
  }

  private InterleavedStaticIOCheck getCheck(String... lines) {
    return new InterleavedIoParser(2)
        .fromString(String.join("\n", lines), "A check");
  }
}
