package me.ialistannen.simplecodetester.submission;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

/**
 * A collection of files that were submitted for checking.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableSubmission.class)
@JsonDeserialize(as = ImmutableSubmission.class)
@Gson.TypeAdapters
public abstract class Submission {

  /**
   * All files in this submission. A mapping from FQN -> Content
   *
   * @return a map of fqn to content for all files
   */
  public abstract Map<String, String> files();
}
