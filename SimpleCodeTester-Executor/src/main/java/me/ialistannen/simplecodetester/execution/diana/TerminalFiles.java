package me.ialistannen.simplecodetester.execution.diana;

import edu.kit.informatik.Terminal;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class TerminalFiles {

  public static String readString(Path input) {
    return String.join(System.lineSeparator(), Terminal.readFile(input.toString()));
  }

  public static List<String> readAllLines(Path input) {
    return Arrays.asList(Terminal.readFile(input.toString()));
  }

  public static boolean exists(Path path, LinkOption... ignored) {
    return Terminal.hasFile(path.toString());
  }

}
