package me.ialistannen.simplecodetester.exceptions;

import me.ialistannen.simplecodetester.compilation.CompilationOutput;

public class CompilationException extends RuntimeException {

  private final CompilationOutput output;

  public CompilationException(CompilationOutput output) {
    super("Compilation failed!");
    this.output = output;
  }

  public CompilationOutput getOutput() {
    return output;
  }
}
