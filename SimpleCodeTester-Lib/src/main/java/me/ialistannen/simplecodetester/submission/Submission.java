package me.ialistannen.simplecodetester.submission;

import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * A collection of files that were submitted for checking.
 */
@Value.Immutable
@Gson.TypeAdapters
public abstract class Submission {

  /**
   * All files in this submission. A mapping from FQN -> Content
   *
   * @return a map of fqn to content for all files
   */
  public abstract Map<String, String> files();
}
