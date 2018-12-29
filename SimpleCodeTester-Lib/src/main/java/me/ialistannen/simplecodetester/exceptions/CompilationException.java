package me.ialistannen.simplecodetester.exceptions;

import java.util.Collections;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import me.ialistannen.simplecodetester.compilation.ImmutableCompilationOutput;

public class CompilationException extends RuntimeException {

  private final CompilationOutput output;

  public CompilationException(CompilationOutput output) {
    super("Compilation failed!");
    this.output = output;
  }

  public CompilationException(String output) {
    this(
        ImmutableCompilationOutput.builder()
            .files(Collections.emptyList())
            .successful(false)
            .diagnostics(Collections.emptyMap())
            .output(output)
            .build()
    );
  }

  public CompilationOutput getOutput() {
    return output;
  }
}
