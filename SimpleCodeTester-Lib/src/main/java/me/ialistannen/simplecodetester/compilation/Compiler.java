package me.ialistannen.simplecodetester.compilation;

import java.io.IOException;
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
   */
  CompiledSubmission compileSubmission(Submission submission) throws IOException;
}
