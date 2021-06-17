package me.ialistannen.simplecodetester.backend.endpoints.runner;

import com.google.gson.Gson;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.services.checkrunning.TaskQueue;
import me.ialistannen.simplecodetester.backend.services.config.RunnerConfig;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import me.ialistannen.simplecodetester.util.ConfiguredGson;
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

    System.out.println("Dishing out work");
    return ResponseEntity.of(taskQueue.pollTask());
  }

  @PostMapping("/report-work")
  public ResponseEntity<CompleteTask> requestWork(
      @RequestHeader("Authorization") String password,
      @RequestBody @NotNull @NotEmpty String resultString
  ) {
    if (!this.runnerConfig.getPassword().equals(password)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Gson gson = ConfiguredGson.createGson();
    AttributedResult result = gson.fromJson(resultString, AttributedResult.class);

    System.out.println("Go results!");
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
