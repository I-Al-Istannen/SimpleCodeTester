package me.ialistannen.simplecodetester.backend.exception;

/**
 * An exception while parsing a check.
 */
public class CheckParseException extends RuntimeException {

  public CheckParseException(String message) {
    super(message);
  }
}
