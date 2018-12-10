package me.ialistannen.simplecodetester.exceptions;

public class CheckFailedException extends RuntimeException {

  public CheckFailedException(String message) {
    super(message);
  }
}
