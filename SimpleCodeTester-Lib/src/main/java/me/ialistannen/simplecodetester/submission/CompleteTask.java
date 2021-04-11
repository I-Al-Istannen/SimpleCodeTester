package me.ialistannen.simplecodetester.submission;

import java.util.List;
import org.immutables.value.Value;

/**
 * A complete task that can be passed to the input of an Executor.
 */
@Value.Immutable
public abstract class CompleteTask {

  public abstract List<String> checks();

  public abstract Submission submission();

  public abstract String userId();
}
