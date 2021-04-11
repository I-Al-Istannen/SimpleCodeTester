package me.ialistannen.simplecodetester.runner;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import me.ialistannen.simplecodetester.checks.defaults.io.InterleavedStaticIOCheck;
import me.ialistannen.simplecodetester.checks.defaults.io.matcher.LiteralIoMatcher;
import me.ialistannen.simplecodetester.checks.storage.CheckSerializer;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.runner.config.RunnerConfiguration;
import me.ialistannen.simplecodetester.runner.execution.Tester;
import me.ialistannen.simplecodetester.submission.ImmutableCompleteTask;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;
import me.ialistannen.simplecodetester.util.ConfiguredGson;

public class Main {

  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Parameter: <config file location>");
      System.exit(1);
    }

    RunnerConfiguration config = loadConfig(args[0]);

    Tester tester = new Tester(
        config.getStartContainerCommand(),
        config.getKillContainerCommand(),
        Duration.ofSeconds(config.getMaxRuntimeSeconds()),
        ConfiguredGson.createGson()
    );
    Result test = tester.test(
        ImmutableCompleteTask.builder()
            .addChecks(new CheckSerializer(ConfiguredGson.createGson()).toJson(
                new InterleavedStaticIOCheck(
                    List.of(), List.of(), List.of(List.of(new LiteralIoMatcher("Hello"))),
                    "Test"
                )
            ))
            .userId("Test")
            .submission(
                ImmutableSubmission.builder()
                    .putFiles(
                        "Test.java",
                        "class Test { public static void main(String... args) {edu.kit.informatik.Terminal.printLine(\"Hello\"); } }"
                    )
                    .build()
            )
            .build()
    );
    System.out.println(test);
  }

  private static RunnerConfiguration loadConfig(String arg) {
    try (BufferedReader reader = Files.newBufferedReader(Path.of(arg))) {
      JsonObject object = ConfiguredGson.createGson().fromJson(reader, JsonObject.class);

      return new RunnerConfiguration(object);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    throw new AssertionError("Never reached!");
  }
}
