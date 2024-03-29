package me.ialistannen.simplecodetester.result;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.ialistannen.simplecodetester.checks.CheckResult;
import me.ialistannen.simplecodetester.compilation.CompilationOutput;
import org.immutables.gson.Gson;
import org.immutables.serial.Serial;
import org.immutables.value.Value;

/**
 * The result of testing a submission.
 */
@Serial.Structural
@Gson.TypeAdapters
@Value.Immutable
public abstract class Result {

  public abstract Map<String, List<CheckResult>> fileResults();

  public abstract Optional<CompilationOutput> compilationOutput();

  public abstract Optional<TimeoutData> timeoutData();

  public abstract Optional<CrashData> crashData();

  @Serial.Structural
  @Value.Immutable
  @Gson.TypeAdapters
  public static abstract class TimeoutData {

    public abstract String lastTest();
  }

  @Serial.Structural
  @Value.Immutable
  @Gson.TypeAdapters
  public static abstract class CrashData {

    public abstract String lastTest();

    public abstract String additionalContext();
  }
}
