package me.ialistannen.simplecodetester.backend.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple string reader.
 */
public class StringReader {

  private String underlying;
  private int cursor;

  /**
   * Creates a new string reader.
   *
   * @param underlying the underlying string
   */
  public StringReader(String underlying) {
    this.underlying = underlying;
  }

  /**
   * Returns the cursor position.
   *
   * @return the cursor position
   */
  public int getCursor() {
    return cursor;
  }

  /**
   * Peeks at the next {@code amount} characters.
   *
   * @param amount the amount of characters to peek for
   * @return the peeked string clamped to the string length or empty if there was nothing to read
   */
  public String peek(int amount) {
    if (!canRead()) {
      return "";
    }
    int targetPosition = clampToRange(amount + cursor);

    return underlying.substring(cursor, targetPosition);
  }

  /**
   * Reads the next {@code amount} chars.
   *
   * @param amount the amount of characters to peek for
   * @return the resd string. See {@link #peek(int)} for details
   */
  public String read(int amount) {
    if (!canRead()) {
      return "";
    }
    int initial = cursor;
    cursor = clampToRange(cursor + amount);

    return underlying.substring(initial, cursor);
  }

  /**
   * Reads all characters matching the given pattern.
   *
   * @param pattern the pattern
   * @return all characters matching the pattern
   */
  public String read(Pattern pattern) {
    Matcher matcher = pattern.matcher(underlying);
    if (!matcher.find(cursor)) {
      return null;
    }

    int length = matcher.end() - matcher.start();
    return read(length);
  }

  /**
   * Reads a single line.
   *
   * @return a single line, null if not found
   */
  public String readLine() {
    String read = read(Pattern.compile(".+$", Pattern.MULTILINE));
    if (peek(1).equalsIgnoreCase("\n")) {
      cursor++;
    }
    return read;
  }

  /**
   * Returns whether the reader has more input to read.
   *
   * @return true if the reader has more input to read.
   */
  public boolean canRead() {
    return cursor < underlying.length();
  }

  private int clampToRange(int position) {
    return Math.max(0, Math.min(position, underlying.length()));
  }
}
