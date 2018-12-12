package edu.kit.informatik;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.joor.Reflect;

/**
 * A shim of the "Terminal" class from the Praktomat.
 */
public final class Terminal {

  private static StringBuilder output = new StringBuilder();
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

  public static void printLine(final Object object) {
    output.append(object)
        .append(System.lineSeparator());
  }

  public static void printLine(final char[] charArray) {
    printLine(String.valueOf(charArray));
  }

  public static String readLine() {
    if (inputIndex >= input.size()) {
      throw new NoSuchElementException("No more input!");
    }
    return input.get(inputIndex++);
  }

  public static String[] readFile(final String path) {
    URL resource = Terminal.class.getResource("/" + path);
    try {
      return Files.readAllLines(Paths.get(resource.toURI())).toArray(String[]::new);
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static void setInput(List<String> input, ClassLoader classLoader) {
    Reflect.on("edu.kit.informatik.Terminal", classLoader)
        .set("input", new ArrayList<>(input))
        .set("inputIndex", 0);
  }

  public static String getOutput(ClassLoader loader) {
    return Reflect.on("edu.kit.informatik.Terminal", loader)
        .get("output")
        .toString();
  }

  /**
   * Resets the terminal state.
   */
  public static void reset() {
    inputIndex = 0;
    output.setLength(0);
  }
}
