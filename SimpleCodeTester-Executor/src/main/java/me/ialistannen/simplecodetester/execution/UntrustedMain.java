package me.ialistannen.simplecodetester.execution;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.security.Policy;
import me.ialistannen.simplecodetester.execution.security.SlavePolicy;
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
    Policy.setPolicy(new SlavePolicy());
    System.setSecurityManager(new SecurityManager());

    Gson gson = ConfiguredGson.createGson();
    Executor executor = new Executor(System.out, gson);
    executor.runTests(readTask(gson));
  }
}
