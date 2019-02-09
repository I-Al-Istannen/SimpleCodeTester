package me.ialistannen.simplecodetester.checks.defaults.io;

import java.util.Objects;

/**
 * A single line in the output.
 */
public class LineResult {

  private final Type type;
  private final String content;

  /**
   * Creates a new line result.
   *
   * @param type the type of the line
   * @param content the content of the line
   */
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LineResult that = (LineResult) o;
    return type == that.type &&
        Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, content);
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
