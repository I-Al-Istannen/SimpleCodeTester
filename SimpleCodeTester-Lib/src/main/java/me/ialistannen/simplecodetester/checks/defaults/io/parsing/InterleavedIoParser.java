package me.ialistannen.simplecodetester.checks.defaults.io.parsing;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedStaticIOCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.LiteralIoMatcher;

/**
 * A parser for {@link InterleavedIoMatcher}s.
 */
public class InterleavedIoParser {

  private List<Parser> parsers;
  private String quitCommand;
  private int minInputLength;

  /**
   * Creates a new interleaved io matcher parser.
   *
   * @param quitCommand the quit command every check needs to have
   * @param minInputLength the minimum input length
   */
  public InterleavedIoParser(String quitCommand, int minInputLength) {
    this.quitCommand = quitCommand;
    this.minInputLength = minInputLength;
    this.parsers = new ArrayList<>();

    this.parsers.add(new LiteralParser());
    this.parsers.add(new CommentParser());
    this.parsers.add(new ErrorParser());
    this.parsers.add(new RegexParser());
    // verbatim is the catch all and *must be last*
    this.parsers.add(new VerbatimParser());
  }

  /**
   * Parses the input and returns a {@link InterleavedStaticIOCheck}.
   *
   * @param input the input to parse
   * @return the check
   * @throws IllegalArgumentException if there is not enough input given
   */
  public InterleavedStaticIOCheck fromString(String input, String name) {
    return fromLines(toLines(input), name);
  }

  private InterleavedStaticIOCheck fromLines(List<Line> lines, String name) {
    List<List<InterleavedIoMatcher>> matchers = new ArrayList<>();
    List<String> input = new ArrayList<>();
    List<String> parameters = new ArrayList<>();

    // Initial content before first input
    matchers.add(new ArrayList<>());

    for (Line line : lines) {
      if (line.type == LineType.OUTPUT) {
        InterleavedIoMatcher matcher = parsers.stream()
            .filter(parser -> parser.canParse(line.value))
            .findFirst()
            .map(parser -> parser.parse(line.value))
            .orElseGet(() -> new LiteralIoMatcher(line.value));

        matchers.get(matchers.size() - 1).add(matcher);
      } else if (line.type == LineType.PARAMETER) {
        parameters.add(line.value);
      } else {
        input.add(line.value);
        matchers.add(new ArrayList<>());
      }
    }

    if (!containsQuitCommand(lines)) {
      input.add(quitCommand);
    }

    if (input.size() < minInputLength) {
      throw new IllegalArgumentException("Expected at least " + minInputLength + " input lines!");
    }

    return new InterleavedStaticIOCheck(input, parameters, matchers, name);
  }

  private boolean containsQuitCommand(List<Line> lines) {
    return lines.stream()
        .filter(line -> line.type == LineType.INPUT)
        .anyMatch(line -> line.value.equals(quitCommand));
  }

  /**
   * Converts a string to a list of {@link Line}s.
   *
   * @param input the input string
   * @return the output lines
   */
  private static List<Line> toLines(String input) {
    return Arrays.stream(input.split("\n"))
        .map(Line::fromString)
        .collect(toList());
  }

  private static class Line {

    private String value;
    private LineType type;

    Line(String value, LineType type) {
      this.value = value;
      this.type = type;
    }

    private static Line fromString(String s) {
      if (s.startsWith("> ")) {
        return new Line(s.substring(2), LineType.INPUT);
      }
      if (s.startsWith("$$ ")) {
        return new Line(s.substring(3), LineType.PARAMETER);
      }

      return new Line(s, LineType.OUTPUT);
    }
  }

  private enum LineType {
    INPUT,
    OUTPUT,
    PARAMETER
  }
}
