package me.ialistannen.simplecodetester.runner.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RunnerConfiguration {

  private final List<String> startContainerCommand;
  private final List<String> killContainerCommand;
  private final int maxRuntimeSeconds;

  public RunnerConfiguration(JsonObject jsonObject) {
    this.startContainerCommand = fromArray(jsonObject.getAsJsonArray("container_start_command"));
    this.killContainerCommand = fromArray(jsonObject.getAsJsonArray("container_kill_command"));
    this.maxRuntimeSeconds = jsonObject.getAsJsonPrimitive("max_runtime_seconds").getAsInt();
  }

  public List<String> getStartContainerCommand() {
    return Collections.unmodifiableList(startContainerCommand);
  }

  public List<String> getKillContainerCommand() {
    return Collections.unmodifiableList(killContainerCommand);
  }

  public int getMaxRuntimeSeconds() {
    return maxRuntimeSeconds;
  }

  private static List<String> fromArray(JsonArray array) {
    return StreamSupport.stream(array.spliterator(), false)
        .map(JsonElement::getAsString)
        .collect(Collectors.toList());
  }
}
