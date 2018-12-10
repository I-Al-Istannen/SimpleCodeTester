package me.ialistannen.simplecodetester.submission;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public abstract class Submission {

  /**
   * Returns all compiled files in this submission.
   *
   * @return all compiled files in this submission
   */
  public abstract List<CompiledFile> files();
}
