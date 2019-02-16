package me.ialistannen.simplecodetester.checks.defaults.io;

import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.InterleavedIoMatcher;

/**
 * A block containing {@link InterleavedIoMatcher}s.
 */
class MatcherBlock {

  private Block<InterleavedIoMatcher> matcherBlock;

  /**
   * Creates a new Matcher block.
   *
   * @param matchers the matchers to use
   */
  MatcherBlock(List<InterleavedIoMatcher> matchers) {
    this.matcherBlock = new Block<>(matchers);
  }

  private MatcherBlock(Block<InterleavedIoMatcher> block) {
    this.matcherBlock = block;
  }

  /**
   * Matches the output and generates a {@link ResultBlock}.
   *
   * @param outputBlock the program output
   * @return the generated {@link ResultBlock}
   */
  ResultBlock match(Block<String> outputBlock) {
    ResultBlock block = new ResultBlock();

    while (matcherBlock.hasNext() && outputBlock.hasNext()) {
      InterleavedIoMatcher matcher = matcherBlock.next();

      outputBlock.mark();

      if (!matcher.match(outputBlock)) {
        outputBlock.getFromLastMark().forEach(block::addOutput);
        block.addError(matcher.getError());
      } else {
        outputBlock.getFromLastMark().forEach(block::addOutput);
      }
    }

    while (matcherBlock.hasNext()) {
      InterleavedIoMatcher matcher = matcherBlock.next();
      block.addError(matcher.getError());
    }

    while (outputBlock.hasNext()) {
      String output = outputBlock.next();
      block.addOutput(output);
      block.addError("Did not expect any output.");
    }

    return block;
  }

  /**
   * Returns whether there is any matcher left.
   *
   * @return true if there is any matcher left
   */
  boolean hasNext() {
    return matcherBlock.hasNext();
  }

  /**
   * Returns the next {@link InterleavedIoMatcher}.
   *
   * @return the next matcher
   */
  InterleavedIoMatcher next() {
    return matcherBlock.next();
  }

  /**
   * Returns a version of this block reading from the start. Might be this instance, might be a new
   * one.
   *
   * @return an instance of this block reading from the start
   */
  MatcherBlock reset() {
    return matcherBlock.isAtStart() ? this : new MatcherBlock(this.matcherBlock.copyFromStart());
  }
}
