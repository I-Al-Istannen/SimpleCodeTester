package me.ialistannen.simplecodetester;

import java.nio.file.Paths;
import me.ialistannen.simplecodetester.execution.SubmissionClassLoader;
import me.ialistannen.simplecodetester.execution.master.SlaveManager;
import me.ialistannen.simplecodetester.submission.ImmutableSubmission;

public class Test {

  public static void main(String[] args) throws Exception {
    SlaveManager slaveManager = new SlaveManager(
        protocolMessage -> System.out.println("Got Master " + protocolMessage)
    );
    slaveManager.start();

    ImmutableSubmission submission = ImmutableSubmission.builder()
        .basePath(Paths.get("/tmp/test"))
        .classLoader(new SubmissionClassLoader(Paths.get("/tmp/test")))
        .build();

    slaveManager.runSubmission(submission, "hello-world");

    Thread.sleep(4000);
    System.out.println("Stopping...");
    slaveManager.stop();
  }
}
