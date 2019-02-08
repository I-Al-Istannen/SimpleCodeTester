package edu.kit.informatik;

import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.ialistannen.simplecodetester.exceptions.ReadMoreLinesThanProvidedException;

/**
 * A shim of the "Terminal" class from the Praktomat.
 */
public final class Terminal {

  private static List<List<String>> output = new ArrayList<>();
  private static List<String> input = Collections.emptyList();
  private static int inputIndex;

  private Terminal() {
    // No instance
    throw new UnsupportedOperationException("Utility class");
  }

  /**
   * Prints the given error-{@code message} with the prefix "{@code Error, }".
   *
   * <p>More specific, this method behaves exactly as if the following code got executed:
   * <blockquote><pre>
   * Terminal.printLine("Error, " + message);</pre>
   * </blockquote>
   *
   * @param message the error message to be printed
   * @see #printLine(Object)
   */
  public static void printError(final String message) {
    printLine("Error, " + message);
  }

  /**
   * Prints a single line.
   *
   * @param object the object to turn to a string and print. Handles nulls gracefully
   */
  public static void printLine(final Object object) {
    if (output.isEmpty()) {
      output.add(new ArrayList<>());
    }
    List<String> outputBlock = output.get(output.size() - 1);

    String asString = Objects.toString(object);
    List<String> lines = new ArrayList<>();
    Collections.addAll(lines, asString.split("\n"));

    // split skips trailing newlines (or just an newline). This means
    // "\n" becomes {} and "hey\n\n" becomes {hey}, but
    // "hey\n\nyou" becomes {hey,,you} (which is correct) and
    // "\nhey" also becomes {,hey} (which is correct)
    // So we need to append at the end until we match the expected line count at the end
    int expectedLines = (int) asString.chars().filter(value -> value == '\n').count();

    while (lines.size() <= expectedLines) {
      // an empty newline
      lines.add("");
    }

    outputBlock.addAll(lines);
  }

  /**
   * Prints a single string, consisting of the given char array.
   *
   * @param charArray the character array to print as a String
   */
  public static void printLine(final char[] charArray) {
    printLine(String.valueOf(charArray));
  }

  /**
   * Reads a single line of input, skipping the newline separator.
   *
   * @return the read line
   */
  public static String readLine() {
    if (inputIndex >= input.size()) {
      throw new ReadMoreLinesThanProvidedException();
    }
    if (output.isEmpty()) {
      output.add(new ArrayList<>());
    }
    output.add(new ArrayList<>());
    return input.get(inputIndex++);
  }

  /**
   * Reads a given file.
   *
   * @param path the name of the file to read
   * @return the contents of the file, with each line being an entry in the array
   */
  public static String[] readFile(final String path) {
    URL resource = Terminal.class.getResource("/" + path);
    try {
      return Files.readAllLines(Paths.get(resource.toURI())).toArray(String[]::new);
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sets the input the terminal class should use.
   *
   * @param input the input with each element representing one line of input
   */
  public static void setInput(List<String> input) {
    Terminal.input = new ArrayList<>(input);
  }

  /**
   * Returns the output that was written in the terminal class.
   *
   * @return the written input
   */
  public static String getOutput() {
    return output.stream()
        .flatMap(Collection::stream)
        .collect(joining("\n"));
  }

  /**
   * Returns the output that was written in the terminal class.
   *
   * @return the output
   */
  public static List<List<String>> getOutputLines() {
    return output;
  }

  /**
   * Resets the terminal's state (read input and written output is cleared).
   */
  public static void reset() {
    inputIndex = 0;
    output = new ArrayList<>();
  }
}
