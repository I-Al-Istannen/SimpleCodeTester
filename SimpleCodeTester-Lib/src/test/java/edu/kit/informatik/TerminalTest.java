package edu.kit.informatik;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerminalTest {

  @BeforeEach
  void setUp() {
    Terminal.reset();
  }

  @Test
  void multilineWorks() {
    String text = "\nHello\nYou\n";
    Terminal.printLine(text);

    assertEquals(
        List.of("", "Hello", "You", ""),
        Terminal.getOutputLines().get(0)
    );
  }

  @Test
  void firstOutputIsBeforeRead() {
    String text = "Hello";
    Terminal.printLine(text);

    assertEquals(
        List.of(List.of("Hello")),
        Terminal.getOutputLines()
    );
  }

  @Test
  void separatesByInput() {
    Terminal.setInput(List.of("", ""));
    List<List<String>> input = List.of(
        List.of("Hello\nYou"),
        List.of("How are you?")
    );

    for (List<String> list : input) {
      list.forEach(Terminal::printLine);
      Terminal.readLine();
    }

    assertEquals(
        List.of(
            List.of("Hello", "You"),
            List.of("How are you?"),
            List.of()
        ),
        Terminal.getOutputLines()
    );
  }

}