package me.ialistannen.simplecodetester.checks.defaults.io;

/**
 * A single line in the output.
 */
public class LineResult {

  private final Type type;
  private final String content;

  public LineResult(Type type, String content) {
    this.type = type;
    this.content = content;
  }

  /**
   * The type of the line.
   *
   * @return the type of the line
   */
  public Type getType() {
    return type;
  }

  /**
   * The content of the line.
   *
   * @return the content of the line
   */
  public String getContent() {
    return content;
  }


  @Override
  public String toString() {
    return String.format("[%-7s] %s", type, content);
  }

  /**
   * The type of the line.
   */
  public enum Type {
    ERROR,
    INPUT,
    OUTPUT
  }
}
