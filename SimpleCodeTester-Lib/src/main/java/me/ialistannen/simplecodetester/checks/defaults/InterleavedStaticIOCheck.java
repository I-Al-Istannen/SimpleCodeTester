package me.ialistannen.simplecodetester.checks.defaults;

import edu.kit.informatik.Terminal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import me.ialistannen.simplecodetester.submission.CompiledFile;

/**
 * A check that executes the code but correlates input to output lines.
 */
public class InterleavedStaticIOCheck extends MainClassRunnerCheck {

  private List<String> input;
  private List<List<InterleavedIoMatcher>> outputMatchers;
  private String name;

  /**
   * Creates a new interleaved static io check.
   *
   * @param input the input
   * @param expectedOutput the expected output
   * @param name the name of the check
   */
  public InterleavedStaticIOCheck(List<String> input,
      List<List<InterleavedIoMatcher>> expectedOutput, String name) {
    this.input = new ArrayList<>(input);
    this.outputMatchers = new ArrayList<>(expectedOutput);
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
    return input;
  }

  @Override
  protected void assertOutputValid(CompiledFile file) {
    getOutput(Terminal.getOutputLines());
  }

  List<LineResult> getOutput(List<List<String>> programOutput) {
    List<LineResult> result = new ArrayList<>();

    int minOutputLength = Math.min(programOutput.size(), outputMatchers.size());

    result.addAll(interleavePresentOutputBlocks(programOutput, minOutputLength));

    if (outputMatchers.size() > programOutput.size()) {
      for (int i = minOutputLength; i < outputMatchers.size(); i++) {
        System.out.println(i + " matcher leftover");
        result.add(new LineResult(Type.INPUT, input.get(i - 1)));
        result.addAll(getLeftoverMatcherLines(outputMatchers.get(i)));
      }
    } else {
      for (int i = minOutputLength; i < programOutput.size(); i++) {
        System.out.println(i + " block leftover");
        result.add(new LineResult(Type.INPUT, input.get(i - 1)));
        result.addAll(getLeftoverOutputLines(programOutput.get(i)));
      }
    }

    return result;
  }

  private List<LineResult> interleavePresentOutputBlocks(List<List<String>> output,
      int minOutputLength) {
    List<LineResult> result = new ArrayList<>();

    for (int blockIndex = 0; blockIndex < minOutputLength; blockIndex++) {
      // first block is before any input
      if (blockIndex > 0) {
        result.add(new LineResult(Type.INPUT, input.get(blockIndex - 1)));
        System.out.println("Added input...");
      }

      List<InterleavedIoMatcher> matcherBlock = outputMatchers.get(blockIndex);
      List<String> outputBlock = output.get(blockIndex);
      int sharedLineLength = Math.min(outputBlock.size(), matcherBlock.size());

      for (int lineIndex = 0; lineIndex < sharedLineLength; lineIndex++) {
        InterleavedIoMatcher matcher = matcherBlock.get(lineIndex);
        String userLine = outputBlock.get(lineIndex);
        result.add(new LineResult(Type.OUTPUT, userLine));

        if (!matcher.match(userLine)) {
          result.add(new LineResult(Type.ERROR, matcher.getError()));
        }
      }

      List<LineResult> leftover = consumeBlockLeftovers(
          matcherBlock.subList(sharedLineLength, matcherBlock.size()),
          outputBlock.subList(sharedLineLength, outputBlock.size())
      );
      result.addAll(leftover);
    }

    return result;
  }

  private List<LineResult> consumeBlockLeftovers(List<InterleavedIoMatcher> matchers,
      List<String> output) {

    if (matchers.isEmpty() && output.isEmpty()) {
      return Collections.emptyList();
    }

    List<LineResult> result = new ArrayList<>();

    if (matchers.size() > output.size()) {
      result.addAll(getLeftoverMatcherLines(matchers));
    } else {
      result.addAll(getLeftoverOutputLines(output));
    }

    return result;
  }

  private List<LineResult> getLeftoverOutputLines(List<String> output) {
    List<LineResult> result = new ArrayList<>(output.size() * 2);

    for (String s : output) {
      result.add(new LineResult(Type.OUTPUT, s));
      result.add(new LineResult(Type.ERROR, "Expected no output"));
    }

    return result;
  }

  private List<LineResult> getLeftoverMatcherLines(List<InterleavedIoMatcher> matchers) {
    List<LineResult> result = new ArrayList<>(matchers.size() * 2);

    for (int i = 0; i < matchers.size(); i++) {
      result.add(new LineResult(Type.ERROR, "Expected no output"));
    }

    return result;
  }

  @Override
  public String toString() {
    return "InterleavedStaticIOCheck{" +
        "input=" + input +
        ", name='" + name + '\'' +
        '}';
  }
}
