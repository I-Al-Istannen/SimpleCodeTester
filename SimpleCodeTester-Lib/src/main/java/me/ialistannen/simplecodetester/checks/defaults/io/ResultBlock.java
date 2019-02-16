package me.ialistannen.simplecodetester.checks.defaults.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;

/**
 * A block storing results.
 */
class ResultBlock {

  private List<LineResult> results;

  /**
   * Creates a new results blocks.
   */
  ResultBlock() {
    this.results = new ArrayList<>();
  }

  /**
   * Adds everything from the given result block to this.
   *
   * @param other the result block to add the results from
   */
  void add(ResultBlock other) {
    results.addAll(other.getResults());
  }

  /**
   * Adds the given line.
   *
   * @param result the result to add
   */
  void add(LineResult result) {
    results.add(result);
  }

  /**
   * Adds an output line.
   *
   * @param output the output to add
   */
  void addOutput(String output) {
    results.add(new LineResult(Type.OUTPUT, output));
  }

  /**
   * Adds a line of undefined type.
   *
   * @param line the line to add
   */
  void addOther(String line) {
    results.add(new LineResult(Type.OTHER, line));
  }

  /**
   * Adds an input line
   *
   * @param input the input
   */
  void addInput(String input) {
    results.add(new LineResult(Type.INPUT, input));
  }

  /**
   * Adds an error line
   *
   * @param error the error
   */
  void addError(String error) {
    results.add(new LineResult(Type.ERROR, error));
  }

  /**
   * Returns all results.
   *
   * @return the results
   */
  List<LineResult> getResults() {
    return Collections.unmodifiableList(results);
  }
}
