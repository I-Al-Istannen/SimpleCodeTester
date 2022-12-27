package me.ialistannen.simplecodetester.submission;

import java.util.List;
import java.util.UUID;
import org.immutables.gson.Gson;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

/**
 * A complete task that can be passed to the input of an Executor.
 */
@Serial.Structural
@Gson.TypeAdapters
@Value.Immutable
public abstract class CompleteTask {

  public abstract List<String> checks();

  public abstract Submission submission();

  public abstract UUID userIdentifier();
}
