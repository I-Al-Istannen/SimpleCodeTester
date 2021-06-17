package me.ialistannen.simplecodetester.result;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
@JsonSerialize(as = ImmutableResult.class)
@JsonDeserialize(as = ImmutableResult.class)
@Gson.TypeAdapters
@Value.Immutable
public abstract class Result {

  public abstract Map<String, List<CheckResult>> fileResults();

  public abstract Optional<CompilationOutput> compilationOutput();

  public abstract Optional<TimeoutData> timeoutData();

  @Serial.Structural
  @Value.Immutable
  @JsonSerialize(as = ImmutableTimeoutData.class)
  @JsonDeserialize(as = ImmutableTimeoutData.class)
  @Gson.TypeAdapters
  public static abstract class TimeoutData {

    public abstract String lastTest();
  }
}
