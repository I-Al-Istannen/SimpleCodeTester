package me.ialistannen.simplecodetester.checks.defaults.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class ResultBlockTest {

  @RepeatedTest(10)
  void addRandomLines() {
    ResultBlock block = new ResultBlock();

    List<LineResult> lines = getRandomLines();
    for (LineResult line : lines) {
      switch (line.getType()) {
        case ERROR:
          block.addError(line.getContent());
          break;
        case OUTPUT:
          block.addOutput(line.getContent());
          break;
        case INPUT:
          block.addInput(line.getContent());
          break;
      }
    }

    assertEquals(
        lines,
        block.getResults()
    );
  }

  private List<LineResult> getRandomLines() {
    List<LineResult> line = new ArrayList<>();

    for (int i = 0; i < 50; i++) {
      int random = ThreadLocalRandom.current().nextInt(3);
      line.add(new LineResult(
          Type.values()[random], "Something " + ThreadLocalRandom.current().nextInt()
      ));
    }

    return line;
  }

  @Test
  void addBlock() {
    ResultBlock block = new ResultBlock();
    block.addInput("Hello");

    ResultBlock other = new ResultBlock();
    other.addInput("world");

    block.add(other);

    assertEquals(
        List.of(new LineResult(Type.INPUT, "Hello"), new LineResult(Type.INPUT, "world")),
        block.getResults()
    );
  }
}