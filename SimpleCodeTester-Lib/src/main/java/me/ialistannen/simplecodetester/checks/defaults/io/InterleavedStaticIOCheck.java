package me.ialistannen.simplecodetester.checks.defaults.io;

import static java.util.stream.Collectors.toList;

import edu.kit.informatik.Terminal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import me.ialistannen.simplecodetester.checks.defaults.MainClassRunnerCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.submission.CompiledFile;

/**
 * A check that executes the code but correlates input to output lines.
 */
public class InterleavedStaticIOCheck extends MainClassRunnerCheck {

  private List<String> input;
  private List<MatcherBlock> outputMatchers;
  private String name;

  /**
   * Constructor for gson deserialization. Otherwise no constructor is invoked and the main
   * predicate is never set up.
   */
  @SuppressWarnings("unused")
  private InterleavedStaticIOCheck() {
  }

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
    this.name = name;

    this.outputMatchers = expectedOutput.stream()
        .map(MatcherBlock::new)
        .collect(toList());
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
    return Collections.unmodifiableList(input);
  }

  @Override
  protected void assertOutputValid(CompiledFile file) {
    List<LineResult> output = getOutput(Terminal.getOutputLines());

    if (output.stream().anyMatch(lineResult -> lineResult.getType() == Type.ERROR)) {
      throw new CheckFailedException(output);
    }
  }

  List<LineResult> getOutput(List<List<String>> programOutput) {
    Block<Block<String>> outputBlocks = new Block<>(
        programOutput.stream()
            .map(Block::new)
            .collect(toList())
    );
    Block<MatcherBlock> matcherBlocks = getMatcherBlock();
    Block<String> inputBlock = new Block<>(input);

    ResultBlock resultBlock = new ResultBlock();

    // pop all blocks that have some output and a corresponding matcher
    while (outputBlocks.hasNext() && matcherBlocks.hasNext()) {
      if (!outputBlocks.isAtStart()) {
        resultBlock.addInput(inputBlock.next());
      }

      MatcherBlock matcherBlock = matcherBlocks.next();
      Block<String> outputBlock = outputBlocks.next();

      resultBlock.add(matcherBlock.match(outputBlock));
    }

    // pop leftover output. Will not run if only matchers are left
    while (outputBlocks.hasNext()) {
      resultBlock.addInput(inputBlock.next());
      Block<String> outputBlock = outputBlocks.next();

      while (outputBlock.hasNext()) {
        resultBlock.addOutput(outputBlock.next());
        resultBlock.addError("Expected no output!");
      }
    }

    // pop leftover matchers. Will not enter if the loop above ran
    while (matcherBlocks.hasNext()) {
      resultBlock.addInput(inputBlock.next());
      MatcherBlock matcherBlock = matcherBlocks.next();

      while (matcherBlock.hasNext()) {
        resultBlock.addError(matcherBlock.next().getError());
      }
    }

    // pop leftover input
    while (inputBlock.hasNext()) {
      resultBlock.addInput(inputBlock.next());
    }

    return resultBlock.getResults();
  }

  private Block<MatcherBlock> getMatcherBlock() {
    return new Block<>(
        outputMatchers.stream().map(MatcherBlock::reset).collect(toList())
    );
  }

  @Override
  protected List<LineResult> getOutput(CompiledFile file) {
    return getOutput(Terminal.getOutputLines());
  }

  @Override
  public String toString() {
    StringJoiner output = new StringJoiner("\n");

    Block<String> inputBlock = new Block<>(input);
    Block<MatcherBlock> matcherBlock = getMatcherBlock();

    while (matcherBlock.hasNext()) {
      if (!matcherBlock.isAtStart()) {
        output.add("> " + inputBlock.next());
      }
      MatcherBlock matchers = matcherBlock.next();
      while (matchers.hasNext()) {
        output.add(matchers.next().toString());
      }
    }

    while (inputBlock.hasNext()) {
      output.add(inputBlock.next());
    }

    return output.toString();
  }
}
