package me.ialistannen.simplecodetester.backend.exception;

import me.ialistannen.simplecodetester.compilation.CompilationOutput;

public class InvalidCheckException extends CodeTesterException {

  private final CompilationOutput output;

  public InvalidCheckException(CompilationOutput output) {
    super("Invalid check");
    this.output = output;
  }

  public InvalidCheckException(String message) {
    super(message);
    this.output = null;
  }

  public CompilationOutput getOutput() {
    return output;
  }
}
