package me.ialistannen.simplecodetester.checks.defaults.io;

import static java.util.stream.Collectors.toList;

import edu.kit.informatik.Terminal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;
import me.ialistannen.simplecodetester.checks.Check;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.checks.ImmutableCheckResult;
import me.ialistannen.simplecodetester.checks.defaults.io.LineResult.Type;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;
import me.ialistannen.simplecodetester.exceptions.CheckFailedException;
import me.ialistannen.simplecodetester.submission.CompiledFile;
import me.ialistannen.simplecodetester.util.ExceptionUtil;
import me.ialistannen.simplecodetester.util.ReflectionHelper;
import org.joor.Reflect;
import org.joor.ReflectException;

/**
 * A check that executes the code but correlates input to output lines.
 */
public class InterleavedStaticIOCheck implements Check {

  private static final Predicate<CompiledFile> isMain = ReflectionHelper
      .hasMain(CompiledFile::asClass);

  private List<String> input;
  private List<MatcherBlock> outputMatchers;
  private String name;
  private List<String> parameters = List.of();
  private List<CheckFile> files = List.of();


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
   * @param parameters the program parameters. Might be empty
   * @param expectedOutput the expected output
   * @param name the name of the check
   */
  public InterleavedStaticIOCheck(List<String> input, List<String> parameters,
      List<List<InterleavedIoMatcher>> expectedOutput, String name) {
    this.parameters = new ArrayList<>(parameters);
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
  public void setFiles(Collection<CheckFile> files) {
    this.files = new ArrayList<>(files);
  }

  @Override
  public CheckResult check(CompiledFile file) {
    if (!isMain.test(file)) {
      return CheckResult.notApplicable(this);
    }

    Terminal.setInput(Collections.unmodifiableList(input));
    Terminal.setInputFiles(Collections.unmodifiableList(files));

    List<LineResult> output;

    try {
      Reflect.onClass(file.asClass())
          .call("main", (Object) parameters.toArray(String[]::new));

      output = getOutput(Terminal.getOutputLines());
    } catch (ReflectException e) {
      List<LineResult> toInterject = ExceptionUtil.getRelevantStackTraceAndMessage(e).stream()
          .map(s -> new LineResult(Type.ERROR, s))
          .collect(toList());
      output = new ArrayList<>(getOutput(Terminal.getOutputLines(), toInterject));
    }

    assertOutputValid(output);

    return ImmutableCheckResult.builder()
        .from(CheckResult.emptySuccess(this))
        .output(output)
        .build();
  }

  /**
   * Asserts that the output is valid. If not throws an appropriate {@link CheckFailedException}.
   *
   * @throws CheckFailedException if the check failed
   */
  private void assertOutputValid(List<LineResult> output) {
    if (output.stream().anyMatch(lineResult -> lineResult.getType() == Type.ERROR)) {
      throw new CheckFailedException(output);
    }
  }

  /**
   * Returns the output interleaved with error messages, input and matcher results.
   *
   * @param programOutput the program output
   * @return an interleaved version of the output, combined with input and error messages
   */
  List<LineResult> getOutput(List<List<String>> programOutput) {
    return getOutput(programOutput, Collections.emptyList());
  }

  /**
   * Returns the output interleaved with error messages, input and matcher results.
   *
   * @param programOutput the program output
   * @param interjectBeforeLeftovers the lines to interject before leftover matchers or input. This
   * can be due to a crash leading to input being left over or something else.
   * @return an interleaved version of the output, combined with input and error messages
   */
  private List<LineResult> getOutput(List<List<String>> programOutput,
      Iterable<LineResult> interjectBeforeLeftovers) {
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
      if (inputBlock.hasNext()) {
        resultBlock.addInput(inputBlock.next());
      }
      Block<String> outputBlock = outputBlocks.next();

      while (outputBlock.hasNext()) {
        resultBlock.addOutput(outputBlock.next());
        resultBlock.addError("Did not expect any output.");
      }
    }

    interjectBeforeLeftovers.forEach(resultBlock::add);

    // pop leftover matchers. Will not enter if the loop above ran
    while (matcherBlocks.hasNext()) {
      if (inputBlock.hasNext()) {
        resultBlock.addInput(inputBlock.next());
      }
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
  public String toString() {
    StringJoiner output = new StringJoiner("\n");

    for (String parameter : parameters) {
      output.add("$$ " + parameter);
    }

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
      output.add("> " + inputBlock.next());
    }

    return output.toString();
  }
}
