package me.ialistannen.simplecodetester.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult;

public class CheckFailedException extends RuntimeException {

  private List<LineResult> outputLines;

  public CheckFailedException(String message) {
    super(message);
    this.outputLines = Collections.emptyList();
  }

  public CheckFailedException(List<LineResult> outputLines) {
    this.outputLines = new ArrayList<>(outputLines);
  }

  /**
   * Returns the output lines. They contain a sequence of INPUT, OUTPUT and ERROR lines.
   *
   * @return the output lines
   */
  public List<LineResult> getOutputLines() {
    return Collections.unmodifiableList(outputLines);
  }
}
