package me.ialistannen.simplecodetester.checks.defaults.io.matcher;

import me.ialistannen.simplecodetester.checks.defaults.io.Block;

/**
 * A matcher for comments.
 */
public class CommentIoMatcher implements InterleavedIoMatcher {

  private String comment;

  /**
   * Creates a new comment matcher.
   *
   * @param comment the comment
   */
  public CommentIoMatcher(String comment) {
    this.comment = comment;
  }

  @Override
  public boolean match(Block<String> output) {
    return true;
  }

  @Override
  public String getError() {
    return null;
  }

  @Override
  public String toString() {
    return "#" + comment;
  }
}
