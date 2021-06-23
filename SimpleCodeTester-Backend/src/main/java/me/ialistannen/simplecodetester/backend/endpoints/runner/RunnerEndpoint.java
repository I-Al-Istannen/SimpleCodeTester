package me.ialistannen.simplecodetester.backend.endpoints.runner;

import java.util.Optional;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.services.checkrunning.TaskQueue;
import me.ialistannen.simplecodetester.backend.services.config.RunnerConfig;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RunnerEndpoint {

  private final TaskQueue taskQueue;
  private final RunnerConfig runnerConfig;

  public RunnerEndpoint(TaskQueue taskQueue, RunnerConfig runnerConfig) {
    this.taskQueue = taskQueue;
    this.runnerConfig = runnerConfig;
  }

  @GetMapping("/request-work")
  public ResponseEntity<CompleteTask> requestWork(@RequestHeader("Authorization") String password) {
    if (!this.runnerConfig.getPassword().equals(password)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Optional<CompleteTask> taskOptional = taskQueue.pollTask();

    taskOptional.ifPresent(task -> log.info("Sending task for ({}) to runner", task.userId()));

    return ResponseEntity.of(taskOptional);
  }

  @PostMapping("/report-work")
  public ResponseEntity<CompleteTask> reportWork(
      @RequestHeader("Authorization") String password,
      @RequestBody @NotNull AttributedResult result
  ) {
    if (!this.runnerConfig.getPassword().equals(password)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    log.info("Received results for ({})", result.getUserId());

    taskQueue.complete(result.getResult(), result.getUserId());
    return ResponseEntity.ok().build();
  }

  private static class AttributedResult {

    private final String userId;
    private final Result result;

    public AttributedResult(String userId, Result result) {
      this.userId = userId;
      this.result = result;
    }

    public String getUserId() {
      return userId;
    }

    public Result getResult() {
      return result;
    }
  }
}
