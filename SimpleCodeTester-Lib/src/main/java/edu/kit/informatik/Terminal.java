package edu.kit.informatik;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import me.ialistannen.simplecodetester.checks.Check.CheckFile;
import me.ialistannen.simplecodetester.exceptions.ReadMoreLinesThanProvidedException;

/**
 * A shim of the "Terminal" class from the Praktomat.
 */
public final class Terminal {

  private static List<List<String>> output = new ArrayList<>();
  private static List<String> input = Collections.emptyList();
  private static Map<String, CheckFile> files = Collections.emptyMap();
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

    // -1 causes trailing newlines to be kept
    Collections.addAll(outputBlock, asString.split("\n", -1));
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
    if (files.containsKey(path)) {
      return files.get(path).getContent().split("\\n");
    }
    throw new IllegalArgumentException("File '" + path + "' not found! I know: " + files.keySet());
  }

  public static boolean hasFile(String path) {
    return files.containsKey(path);
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
   * Sets the input files the terminal class should use.
   *
   * @param files the input files
   */
  public static void setInputFiles(List<CheckFile> files) {
    Terminal.files = files.stream().collect(toMap(CheckFile::getName, it -> it));
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
   * @return true if there is unread input left, i.e. the program didn't read everything it could
   */
  public static boolean hasLeftoverInput() {
    return inputIndex < input.size();
  }

  /**
   * @return the index of the last read input. -1 if nothing was read
   */
  public static int getLastReadInputIndex() {
    return inputIndex - 1;
  }

  /**
   * Resets the terminal's state (read input and written output is cleared).
   */
  public static void reset() {
    inputIndex = 0;
    output = new ArrayList<>();
  }
}
