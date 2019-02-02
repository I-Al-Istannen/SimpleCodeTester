package me.ialistannen.simplecodetester.checks.defaults;

import static java.util.stream.Collectors.toList;

import edu.kit.informatik.Terminal;
import java.util.Arrays;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.submission.CompiledFile;

/**
 * A MainClassRunnerCheck that executes all classes with a main method by default and verifies that
 * their output for a given input is correct.
 *
 * The output and input are static, not changing and supplied in the constructor.
 */
public class StaticInputOutputCheck extends MainClassRunnerCheck {

  private static final String REGEX_MARKER = "?r";

  private List<String> input;
  private String expectedOutput;
  private String name;

  public StaticInputOutputCheck(List<String> input, String expectedOutput, String name) {
    this.input = input;
    this.expectedOutput = expectedOutput;
    this.name = name;
  }

  @Override
  protected List<String> getInput(CompiledFile file) {
    return input;
  }

  @Override
  public String name() {
    return name;
  }

  /**
   * Return the input.
   *
   * @return the input
   */
  public List<String> getInput() {
    return input;
  }

  /**
   * The expected output.
   *
   * @return the expected output
   */
  public String getExpectedOutput() {
    return expectedOutput;
  }

  @Override
  protected void assertOutputValid(CompiledFile file) {
    String actualOutput = Terminal.getOutput();

    String[] outputLines = stripTrailingNewlines(actualOutput).split("\n");
    String[] expectedLines = stripTrailingNewlines(expectedOutput).split("\n");

    if (expectedLines.length != outputLines.length) {
      throw new CheckFailedException(String.format(
          "Output length does not match (got %d, expected %d).\n\n"
              + "Full input:\n'%s'\n\n"
              + "Full output:\n'%s'\n\n"
              + "Expected output:\n'%s'",
          outputLines.length, expectedLines.length, String.join("\n", input),
          actualOutput, expectedOutput
      ));
    }

    for (int i = 0; i < outputLines.length; i++) {
      String outputLine = outputLines[i];
      if (!lineMatches(outputLine, expectedLines[i])) {
        throw new CheckFailedException(String.format(
            "Output did not match at line %d, expected: '%s', got '%s'.\n\n"
                + "Full input:\n'%s'\n\n"
                + "Full output:\n'%s'\n\n"
                + "Expected output:\n'%s'",
            i, expectedLines[i], outputLine, String.join("\n", input),
            actualOutput, expectedOutput
        ));
      }
    }
  }

  private boolean lineMatches(String actualLine, String expectedLine) {
    if (!expectedLine.startsWith(REGEX_MARKER)) {
      return actualLine.equals(expectedLine);
    }
    return actualLine.matches(expectedLine.substring(REGEX_MARKER.length()));
  }

  private String stripTrailingNewlines(String input) {
    for (int i = input.length() - 1; i >= 0; i--) {
      if (input.charAt(i) >= ' ') {
        return input.substring(0, i + 1);
      }
    }

    return "";
  }

  @Override
  protected List<LineResult> getOutput(CompiledFile file) {
    return Arrays.stream(Terminal.getOutput().split("\n"))
        .map(s -> new LineResult(Type.OUTPUT, s))
        .collect(toList());
  }

  @Override
  public String toString() {
    return "StaticInputOutputCheck{" +
        "input=" + input +
        ", expectedOutput='" + expectedOutput + '\'' +
        ", name='" + name + '\'' +
        '}';
  }
}
