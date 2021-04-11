package me.ialistannen.simplecodetester.execution.compilation;

import me.ialistannen.simplecodetester.exceptions.CompilationException;
import me.ialistannen.simplecodetester.submission.CompiledSubmission;
import me.ialistannen.simplecodetester.submission.Submission;

/**
 * A compiler that is able to compile a {@link Submission}.
 */
public interface Compiler {

  /**
   * Compiles the given {@link Submission}.
   *
   * @param submission the {@link Submission} to compile
   * @return the compiled submission
   * @throws CompilationException if an error occurs while compiling the submission
   */
  CompiledSubmission compileSubmission(Submission submission);
}
