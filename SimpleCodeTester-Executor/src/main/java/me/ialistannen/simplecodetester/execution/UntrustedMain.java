package me.ialistannen.simplecodetester.execution;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.security.Policy;
import me.ialistannen.simplecodetester.execution.security.ExecutorSandboxPolicy;
import me.ialistannen.simplecodetester.submission.CompleteTask;
import me.ialistannen.simplecodetester.util.ConfiguredGson;

/**
 * The main class of the untrusted jvm.
 */
public final class UntrustedMain {

  private static CompleteTask readTask(Gson gson) {
    return gson.fromJson(new InputStreamReader(System.in), CompleteTask.class);
  }

  public static void main(String[] args) {
    Policy.setPolicy(new ExecutorSandboxPolicy());
    System.setSecurityManager(new SecurityManager());

    Gson gson = ConfiguredGson.createGson();
    Executor executor = new Executor(System.out, gson);
    CompleteTask task = readTask(gson);
    executor.runTests(task);
  }
}
