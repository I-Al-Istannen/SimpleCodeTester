package me.ialistannen.simplecodetester.checks.defaults.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.LiteralIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.RegularExpressionIoMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MatcherBlockTest {

  private MatcherBlock block;

  @BeforeEach
  void setup() {
    block = new MatcherBlock(List.of(
        new LiteralIoMatcher("Hello"),
        new RegularExpressionIoMatcher(".+")
    ));
  }

  @Test
  void iterationWorks() {
    assertTrue(block.hasNext());
    assertEquals(new LiteralIoMatcher("Hello"), block.next());

    assertTrue(block.hasNext());
    assertEquals(new RegularExpressionIoMatcher(".+"), block.next());

    assertFalse(block.hasNext());
  }

  @Test
  void reset() {
    block.next();
    block.next();

    assertFalse(block.hasNext(), "Has more input");

    MatcherBlock resetBlock = block.reset();

    assertFalse(block.hasNext(), "Changed original");
    assertEquals(new LiteralIoMatcher("Hello"), resetBlock.next());
  }

  @Test
  void perfectInput() {
    Block<String> input = new Block<>(List.of("Hello", "you there"));

    ResultBlock result = block.match(input);

    assertEquals(
        result.getResults(),
        List.of(
            new LineResult(Type.OUTPUT, "Hello"),
            new LineResult(Type.OUTPUT, "you there")
        )
    );
  }

  @Test
  void wrongOutputForFirst() {
    Block<String> input = new Block<>(List.of("world", "you there"));

    ResultBlock result = block.match(input);

    assertEquals(
        result.getResults(),
        List.of(
            new LineResult(Type.OUTPUT, "world"),
            new LineResult(Type.ERROR, "Expected 'Hello'"),
            new LineResult(Type.OUTPUT, "you there")
        )
    );
  }

  @Test
  void notEnoughOutput() {
    Block<String> input = new Block<>(List.of("Hello"));

    ResultBlock result = block.match(input);

    assertEquals(
        result.getResults(),
        List.of(
            new LineResult(Type.OUTPUT, "Hello"),
            new LineResult(Type.ERROR, "Expected a match for pattern '.+'")
        )
    );
  }

  @Test
  void tooMuchOutput() {
    Block<String> input = new Block<>(List.of("Hello", "you there", "my friend", "golem"));

    ResultBlock result = block.match(input);

    assertEquals(
        result.getResults(),
        List.of(
            new LineResult(Type.OUTPUT, "Hello"),
            new LineResult(Type.OUTPUT, "you there"),
            new LineResult(Type.OUTPUT, "my friend"),
            new LineResult(Type.ERROR, "Did not expect any output."),
            new LineResult(Type.OUTPUT, "golem"),
            new LineResult(Type.ERROR, "Did not expect any output.")
        )
    );
  }

}