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

  /**
   * Creates a new interleaved io matcher parser.
   */
  public InterleavedIoParser() {
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
   */
  public InterleavedStaticIOCheck fromString(String input, String name) {
    return fromLines(toLines(input), name);
  }

  private InterleavedStaticIOCheck fromLines(List<Line> lines, String name) {
    List<List<InterleavedIoMatcher>> matchers = new ArrayList<>();
    List<String> input = new ArrayList<>();

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
      } else {
        input.add(line.value);
        matchers.add(new ArrayList<>());
      }
    }

    return new InterleavedStaticIOCheck(input, matchers, name);
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

      return new Line(s, LineType.OUTPUT);
    }
  }

  private enum LineType {
    INPUT,
    OUTPUT
  }
}
