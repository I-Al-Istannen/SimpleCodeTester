package me.ialistannen.simplecodetester.submission;

import java.nio.file.Path;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import org.immutables.value.Value;

/**
 * A collection of files that were submitted for checking.
 */
@Value.Immutable
public abstract class Submission {

  /**
   * Returns the base path where files for this submission are stored.
   *
   * @return base path where files for this submission are stored
   */
  public abstract Path basePath();

  /**
   * Returns the {@link SubmissionClassLoader} to use for this submission.
   *
   * @return the submission class loader to use
   */
  public abstract SubmissionClassLoader classLoader();
}
