package me.ialistannen.simplecodetester.backend.exception;

import me.ialistannen.simplecodetester.compilation.CompilationOutput;

public class CompilationFailedException extends CodeTesterException {

  private final CompilationOutput output;

  public CompilationFailedException(CompilationOutput output) {
    super("Compilation failed");
    this.output = output;
  }

  public CompilationOutput getOutput() {
    return output;
  }
}
