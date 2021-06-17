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
  private final String backendUrl;
  private final String password;

  public RunnerConfiguration(JsonObject jsonObject) {
    this.startContainerCommand = fromArray(jsonObject.getAsJsonArray("container_start_command"));
    this.killContainerCommand = fromArray(jsonObject.getAsJsonArray("container_kill_command"));
    this.maxRuntimeSeconds = jsonObject.getAsJsonPrimitive("max_runtime_seconds").getAsInt();
    this.password = jsonObject.getAsJsonPrimitive("backend_password").getAsString();
    this.backendUrl = jsonObject.getAsJsonPrimitive("backend_url").getAsString();
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

  public String getBackendUrl() {
    return backendUrl;
  }

  public String getPassword() {
    return password;
  }

  private static List<String> fromArray(JsonArray array) {
    return StreamSupport.stream(array.spliterator(), false)
        .map(JsonElement::getAsString)
        .collect(Collectors.toList());
  }
}
