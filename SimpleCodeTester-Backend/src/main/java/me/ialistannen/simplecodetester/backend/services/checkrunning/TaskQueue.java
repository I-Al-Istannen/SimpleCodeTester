package me.ialistannen.simplecodetester.backend.services.checkrunning;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.checks.CheckResult.ResultType;
import me.ialistannen.simplecodetester.checks.ImmutableCheckResult;
import me.ialistannen.simplecodetester.result.ImmutableResult;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Slf4j
public class TaskQueue {

  private final Deque<CompleteTask> tasks;
  private final Map<String, CompletableFuture<Result>> pendingElements;
  private final Map<UUID, String> userIdMap;

  public TaskQueue() {
    this.tasks = new ArrayDeque<>();
    this.pendingElements = new HashMap<>();
    this.userIdMap = new LinkedHashMap<>() {
      @Override
      protected boolean removeEldestEntry(Entry<UUID, String> eldest) {
        return size() > 10_000;
      }
    };
  }

  /**
   * Adds the given task to the queue.
   *
   * If there already is a task for the user, the task will be replaced with the given one. If the
   * existing task was already handed off to the runner, the task is ignored and the future for the
   * running task is returned instead.
   *
   * Under no circumstances will two tasks for the same user exist in this queue.
   *
   * @param task the task to add
   * @return a future that will be completed with the result of executing the task
   */
  public synchronized CompletableFuture<Result> addTask(String userId, CompleteTask task) {
    log.info("Queuing task for ({})", userId);

    boolean hasPendingTask = pendingElements.containsKey(userId);

    if (hasPendingTask) {
      log.info("User ({}) submitted a new task while the old one was still pending", userId);
      completeWithError(
          pendingElements.get(userId),
          "You made a new request. "
          + "This one will be discarded and you were placed at the end of the queue."
      );
    }

    userIdMap.put(task.userIdentifier(), userId);
    CompletableFuture<Result> future = new CompletableFuture<>();
    tasks.addFirst(task);
    pendingElements.put(userId, future);

    return future;
  }

  /**
   * Removes the task for the given user, if any exists. If the task is currently dispatched to a
   * runner, the result of the computation will be ignored.
   *
   * Any pending future will be completed with an error result.
   *
   * @param userId the id of the user to remove the task for
   */
  public synchronized void removeTaskForUser(String userId) {
    tasks.removeIf(task -> {
      String id = userIdMap.get(task.userIdentifier());
      if (id == null) {
        return true;
      }
      return id.equals(userId);
    });
    CompletableFuture<Result> future = pendingElements.remove(userId);

    if (future != null) {
      completeWithError(future, "The check was cancelled");
    }
  }

  private void completeWithError(CompletableFuture<Result> future, String message) {
    future.complete(ImmutableResult.builder()
        .fileResults(Map.of(
            "Errors", List.of(
                ImmutableCheckResult.builder()
                    .result(ResultType.FAILED)
                    .check("Cancellation")
                    .message(message)
                    .errorOutput("")
                    .build()
            )
        ))
        .build()
    );
  }

  /**
   * Fetches and removes the next task in thr queue.
   *
   * @return the next task in the queue
   */
  public synchronized Optional<CompleteTask> pollTask() {
    return Optional.ofNullable(tasks.pollFirst());
  }

  /**
   * Completes a pending future with the check result.
   *
   * @param result the check result
   * @param userIdentifier the user the result belongs to
   */
  public synchronized void complete(Result result, UUID userIdentifier) {
    String userId = userIdMap.get(userIdentifier);
    CompletableFuture<Result> future = pendingElements.remove(userId);
    if (future != null) {
      future.complete(result);
    } else {
      log.warn("Ignored result for ({}) as the future was already null", userId);
    }
  }

  /**
   * @return the current amount of tasks still waiting to be executed
   */
  public synchronized int size() {
    return this.tasks.size();
  }
}
