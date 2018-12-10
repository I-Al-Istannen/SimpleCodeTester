package me.ialistannen.simplecodetester.submission;

import java.nio.file.Path;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import org.immutables.value.Value;

@Value.Immutable
public abstract class Submission {

  /**
   * Returns the base path where files for this submission are stored.
   *
   * @return base path where files for this submission are stored
   */
  public abstract Path basePath();

  /**
   * Returns the {@link SubmissionClassLoader} to use.
   *
   * @return the submission class loader to use
   */
  public abstract SubmissionClassLoader classLoader();
}
