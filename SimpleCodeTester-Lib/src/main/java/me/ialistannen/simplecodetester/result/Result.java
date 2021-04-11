package me.ialistannen.simplecodetester.result;

import java.util.List;
import java.util.Optional;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import org.immutables.value.Value;

/**
 * The result of testing a submission.
 */
@Value.Immutable
public abstract class Result {

  public abstract List<CheckResult> results();

  public abstract Optional<CompilationOutput> compilationOutput();

  public abstract Optional<TimeoutData> timeoutData();


  @Value.Immutable
  public static abstract class TimeoutData {

    public abstract String lastTest();
  }
}
