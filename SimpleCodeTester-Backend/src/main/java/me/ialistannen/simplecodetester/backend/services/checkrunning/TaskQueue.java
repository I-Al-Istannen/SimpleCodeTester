package me.ialistannen.simplecodetester.backend.services.checkrunning;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import me.ialistannen.simplecodetester.result.Result;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class TaskQueue {

  private final Deque<CompleteTask> tasks;
  private final Map<String, CompletableFuture<Result>> pendingElements;

  public TaskQueue() {
    this.tasks = new ArrayDeque<>();
    this.pendingElements = new HashMap<>();
  }

  public synchronized CompletableFuture<Result> addTask(CompleteTask task) {
    System.out.println("Adding " + task.userId());
    CompletableFuture<Result> future = new CompletableFuture<>();
    tasks.addFirst(task);
    pendingElements.put(task.userId(), future);

    return future;
  }

  public synchronized Optional<CompleteTask> pollTask() {
    return Optional.ofNullable(tasks.pollFirst());
  }

  public synchronized void complete(Result result, String userId) {
    System.out.println("Completing " + userId);
    pendingElements.remove(userId).complete(result);
  }

  public synchronized int size() {
    return this.tasks.size();
  }
}
