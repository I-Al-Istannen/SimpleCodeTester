package me.ialistannen.simplecodetester.exceptions;

public class CheckFailedException extends RuntimeException {

  public CheckFailedException(String message) {
    super(message);
  }

  public CheckFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
