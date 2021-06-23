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
   * All files in this submission. A mapping from FQN (in java file syntax,
   * so "de/ialistannen/Foo.java" -> Content.
   */
  public abstract Map<String, String> files();
}
