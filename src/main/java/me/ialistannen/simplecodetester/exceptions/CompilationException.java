package me.ialistannen.simplecodetester.exceptions;

public class CompilationException extends RuntimeException {

  public CompilationException(String message, Throwable cause) {
    super(message, cause);
  }
}
