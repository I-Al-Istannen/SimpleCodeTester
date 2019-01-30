package me.ialistannen.simplecodetester.checks.defaults;

import static java.util.stream.Collectors.toList;

import edu.kit.informatik.Terminal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.util.Pair;

/**
 * A check that executes the code but correlates input to output lines.
 */
public class InterleavedStaticIOCheck extends MainClassRunnerCheck {

  private List<Pair<String, List<String>>> input;
  private String name;

  /**
   * Creates a new interleaved static io check.
   *
   * @param input the data
   * @param name the name of the check
   */
  public InterleavedStaticIOCheck(List<Pair<String, List<String>>> input, String name) {
    this.input = new ArrayList<>(input);
    this.name = name;
  }

  @Override
  public boolean needsApproval() {
    return false;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  protected List<String> getInput(CompiledFile file) {
    return input.stream().map(Pair::getKey).collect(toList());
  }

  @Override
  protected void assertOutputValid(CompiledFile file) {
    String output = Terminal.getOutput();
    String concat = input.stream()
        .map(Pair::getValue)
        .flatMap(Collection::stream)
        .collect(Collectors.joining("\n"));

    if (!output.equalsIgnoreCase(concat)) {
      throw new CheckFailedException("Failed!");
    }
  }

  @Override
  public String toString() {
    return "InterleavedStaticIOCheck{" +
        "input=" + input +
        ", name='" + name + '\'' +
        '}';
  }
}
