package me.ialistannen.simplecodetester.runner;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.runner.communication.BackendCommunicator;
import me.ialistannen.simplecodetester.runner.config.RunnerConfiguration;
import me.ialistannen.simplecodetester.runner.execution.Tester;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws InterruptedException {
    if (args.length < 1) {
      System.err.println("Usage: Runner <config file location>");
      System.exit(1);
    }

    RunnerConfiguration config = loadConfig(args[0]);

    Tester tester = new Tester(
        config.getStartContainerCommand(),
        config.getKillContainerCommand(),
        Duration.ofSeconds(config.getMaxRuntimeSeconds()),
        ConfiguredGson.createGson()
    );
    BackendCommunicator backendCommunicator = new BackendCommunicator(
        config.getBackendUrl(), config.getPassword(), ConfiguredGson.createGson()
    );

    //noinspection InfiniteLoopStatement
    while (true) {
      try {
        iteration(tester, backendCommunicator);
      } catch (ConnectException e) {
        LOGGER.warn("Connection refused: {}", e.getMessage());
      } catch (IOException | InterruptedException e) {
        LOGGER.warn("Error in iteration", e);
      }

      //noinspection BusyWait
      Thread.sleep(2000);
    }
  }

  private static void iteration(Tester tester, BackendCommunicator backendCommunicator)
      throws IOException, InterruptedException {
    Optional<CompleteTask> taskOpt = backendCommunicator.requestTask();

    if (taskOpt.isEmpty()) {
      return;
    }
    CompleteTask task = taskOpt.get();
    LOGGER.info("Got work for '{}'", task.userIdentifier());
    Instant start = Instant.now();

    Result result = tester.test(task);

    Duration duration = Duration.between(start, Instant.now());
    LOGGER.info(
        "Sending results for '{}'. Tests took {}s {}ms",
        task.userIdentifier(),
        duration.toSeconds(),
        duration.toMillisPart()
    );

    backendCommunicator.sendResults(result, task.userIdentifier());
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
