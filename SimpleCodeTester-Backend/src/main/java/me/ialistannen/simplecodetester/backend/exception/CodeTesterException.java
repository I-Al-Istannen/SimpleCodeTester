package me.ialistannen.simplecodetester.backend.exception;

public class CodeTesterException extends RuntimeException {

  public CodeTesterException(String message) {
    super(message);
  }

  public CodeTesterException(String message, Throwable cause) {
    super(message, cause);
  }
}
