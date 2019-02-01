package me.ialistannen.simplecodetester.checks.defaults;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import edu.kit.informatik.Terminal;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.ErrorIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import me.ialistannen.simplecodetester.checks.defaults.io.RegularExpressionIoMatcher;
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
        List.of("Hello"),
        List.of(
            List.of(), // nothing before first read
            List.of(new ErrorIoMatcher())
        )
    );

    List<LineResult> result = check.getOutput(List.of(
        List.of(), // nothing before first read
        List.of("Error, wrong input")
    ));

    assertHasOrder(result, Type.INPUT, Type.OUTPUT);
  }

  @Test
  void matchingOutputHasWrongOutput() {
    InterleavedStaticIOCheck check = getCheck(
        List.of("Hello"),
        List.of(
            List.of(), // nothing before first read
            List.of(new ErrorIoMatcher())
        )
    );

    List<LineResult> result = check.getOutput(List.of(
        List.of(), // nothing before first read
        List.of("Correct input")
    ));

    assertHasOrder(result, Type.INPUT, Type.OUTPUT, Type.ERROR);
  }

  @Test
  void fusedOutput() {
    InterleavedStaticIOCheck check = getCheck(
        List.of("a", "b", "c"),
        List.of(
            List.of(
                new RegularExpressionIoMatcher("a"),
                new RegularExpressionIoMatcher("b"),
                new RegularExpressionIoMatcher("c")
            ),
            List.of(
                new RegularExpressionIoMatcher("a"),
                new RegularExpressionIoMatcher("b")
            ),
            List.of(
                new RegularExpressionIoMatcher("a")
            )
        )
    );

    List<LineResult> result = check.getOutput(List.of(
        List.of("a"),
        List.of("a", "b"),
        List.of("a", "b", "c")
    ));

    for (LineResult lineResult : result) {
      System.out.println(lineResult);
    }
    assertHasOrder(
        result,
        Type.OUTPUT, Type.ERROR, Type.ERROR,
        Type.INPUT,
        Type.OUTPUT, Type.OUTPUT,
        Type.INPUT,
        Type.OUTPUT, Type.OUTPUT, Type.ERROR, Type.OUTPUT, Type.ERROR,
        Type.INPUT, Type.ERROR
    );
  }

  private InterleavedStaticIOCheck getCheck(List<String> input,
      List<List<InterleavedIoMatcher>> matcher) {

    return new InterleavedStaticIOCheck(input, matcher, "test");
  }

  private void assertHasOrder(List<LineResult> results, LineResult.Type... expected) {
    assertThat(
        results.stream().map(LineResult::getType).toArray(LineResult.Type[]::new),
        is(expected)
    );
  }
}