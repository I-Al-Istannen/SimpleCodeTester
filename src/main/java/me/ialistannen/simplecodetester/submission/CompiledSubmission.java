package me.ialistannen.simplecodetester.submission;

import java.util.List;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import org.immutables.value.Value;

@Value.Immutable
public abstract class CompiledSubmission extends Submission {

  /**
   * Returns all compiled files in this submission.
   *
   * @return all compiled files in this submission
   */
  public abstract List<CompiledFile> files();

  /**
   * Returns the compilation output.
   *
   * @return the compilation output
   */
  public abstract CompilationOutput compilationOutput();
}
